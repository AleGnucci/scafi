/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.simulation.gui.controller.controller3d

import java.awt.Image

import it.unibo.scafi.simulation.gui.controller.controller3d.helper.ControllerStarter
import it.unibo.scafi.simulation.gui.controller.controller3d.helper.NodeUpdater.updateNode
import it.unibo.scafi.simulation.gui.controller.{Controller, ControllerUtils}
import it.unibo.scafi.simulation.gui.model._
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.view.ConfigurationPanel
import it.unibo.scafi.simulation.gui.view.ui3d.{DefaultSimulatorUI3D, SimulatorUI3D}
import it.unibo.scafi.simulation.gui.{Settings, Simulation}
import javax.swing.{JFrame, SwingUtilities}

class DefaultController3D(simulation: Simulation, simulationManager: SimulationManager)
  extends Controller3D with Controller{
  private var gui: SimulatorUI3D = _
  private var nodeValueTypeToShow: NodeValue = NodeValue.EXPORT

  def startup(): Unit = {
    simulation.setController(this)
    startGUI()
    ControllerUtils.addPopupObservations(gui.customPopupMenu,
      () => gui.getSimulationPanel.toggleConnections(), this)
    ControllerUtils.setupSensors(Settings.Sim_Sensors)
    startSimulation()
  }

  private def startGUI(): Unit = SwingUtilities.invokeAndWait(() => {
    gui = DefaultSimulatorUI3D(this)
    ControllerStarter.setupGUI(gui)
    if (Settings.ShowConfigPanel) new ConfigurationPanel(this)
  })

  override def getUI: JFrame = gui

  def setShowValue(valueType: NodeValue): Unit = {this.nodeValueTypeToShow = valueType}

  def getShowValue: NodeValue = this.nodeValueTypeToShow

  override def startSimulation(): Unit = {
    simulationManager.setUpdateNodeFunction(updateNode(_, gui, simulation, () => getShowValue))
    ControllerStarter.startSimulation(simulation, gui, simulationManager)
  }

  override def stopSimulation(): Unit = simulationManager.stop()

  override def pauseSimulation(): Unit = simulationManager.pause()

  override def resumeSimulation(): Unit = simulationManager.resume()

  override def stepSimulation(stepCount: Int): Unit = simulationManager.step(stepCount)

  override def clearSimulation(): Unit = {
    simulationManager.stop()
    ControllerUtils.enableMenuBar(enable = false, gui.getJMenuBar)
    gui.reset()
  }

  def handleNumberButtonPress(sensorIndex: Int): Unit = {
    getSensorName(sensorIndex).foreach(sensorName => {
      val selectedNodeIDs = gui.getSimulationPanel.getSelectedNodesIDs
      val selectedNodes = simulation.network.nodes.filter(node => selectedNodeIDs.contains(node._2.id.toString)).values
      gui.getSimulationPanel.getInitialSelectedNodeId.foreach(initialNodeId => {
        val initialNode = selectedNodes.filter(_.id == initialNodeId.toInt).head
        val sensorValue = initialNode.getSensorValue(sensorName)
        val newSensorValue = sensorValue match {case value: Boolean => !value}
        selectedNodeIDs.foreach(setNodeSensor(_, sensorName, newSensorValue))
        simulation.setSensor(sensorName, newSensorValue, selectedNodes.toSet)
      })
    })
  }

  private def getSensorName(sensorIndex: Int): Option[String] = SensorEnum.fromInt(sensorIndex).map(_.name)

  private def setNodeSensor(nodeId: String, sensorName: String, newSensorValue: Boolean): Unit = {
    val selectedNode = simulation.network.nodes(nodeId.toInt)
    selectedNode.setSensor(sensorName, newSensorValue)
    val firstEnabledSensorInNode = selectedNode.sensors.filter(_._2.equals(true)).keys.headOption
    val sensorColor = firstEnabledSensorInNode.map(SensorEnum.getColor(_).getOrElse(Settings.Color_device))
    gui.getSimulationPanel.setModifiedNodesColor(sensorColor.getOrElse(Settings.Color_device))
  }

  def shutDown(): Unit = System.exit(0)

  def decreaseFontSize(): Unit = gui.getSimulationPanel.decreaseFontSize()

  def increaseFontSize(): Unit = gui.getSimulationPanel.increaseFontSize()

  def slowDownSimulation(): Unit = simulationManager.simulation.setDeltaRound(getSimulationDeltaRound + 10)

  private def getSimulationDeltaRound: Double = simulationManager.simulation.getDeltaRound()

  def speedUpSimulation(): Unit = {
    val currentDeltaRound = getSimulationDeltaRound
    val newValue = if(currentDeltaRound-10 < 0) 0 else currentDeltaRound-10
    simulationManager.simulation.setDeltaRound(newValue)
  }

  override def selectionAttempted: Boolean = gui.getSimulationPanel.isAttemptingSelection

  override def showImage(img: Image, showed: Boolean): Unit = () //do nothing
  override def setObservation(observation: Any => Boolean): Unit = () //do nothing
}

object DefaultController3D {
  def apply(simulation: Simulation, simulationManager: SimulationManager): DefaultController3D =
    new DefaultController3D(simulation, simulationManager)
}
