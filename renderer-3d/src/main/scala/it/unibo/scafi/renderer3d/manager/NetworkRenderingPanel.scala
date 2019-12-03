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

package it.unibo.scafi.renderer3d.manager

import it.unibo.scafi.renderer3d.camera.{FpsCamera, SimulationCamera}
import it.unibo.scafi.renderer3d.util.Rendering3DUtils
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.embed.swing.JFXPanel
import javafx.scene.input
import javafx.scene.input.{MouseButton, MouseEvent}
import scalafx.scene.input.KeyEvent
import scalafx.scene.{Group, Scene, SceneAntialiasing}

final class NetworkRenderingPanel() extends JFXPanel
  with ConnectionManager with NodeManager with SelectionManager {

  override protected val mainScene: Scene = new Scene(createScene())
  this.setScene(mainScene)

  private[this] def createScene(): Scene = {
    new Scene(0, 0, true, SceneAntialiasing.Balanced) {
      val simulationCamera: SimulationCamera = FpsCamera()
      camera = simulationCamera
      root = new Group(Seq(simulationCamera) ++ Rendering3DUtils.createLights: _*)
      setKeyboardInteraction(this, simulationCamera)
      setMouseInteraction(this, simulationCamera)
    }
  }

  private[this] def setKeyboardInteraction(scene: Scene, camera: SimulationCamera): Unit =
    scene.addEventFilter(KeyEvent.KeyPressed, (event: input.KeyEvent) => {
      if (camera.isKeyboardEventAMovement(event)) {
        if(camera.getPosition.magnitude()%2 < 0.5) rotateAllNodeLabels(camera)
      }
      camera.moveByKeyboardEvent(event)
      camera.zoomByKeyboardEvent(event)
    })

  private[this] def setMouseInteraction(scene: Scene, camera: SimulationCamera): Unit = {
    scene.setOnDragDetected(_ => scene.startFullDrag())
    scene.setOnMousePressed(event => if(isPrimaryButton(event)) setSelectionVolumeCenter(event))
    scene.onMouseDragEntered = event => if(isPrimaryButton(event)) startSelection(event)
    scene.onMouseDragged = event => {
      if(isPrimaryButton(event)) modifySelectionVolume(camera, event)
      if(event.getButton == MouseButton.SECONDARY) camera.rotateByMouseEvent(event)
    }
    scene.onMouseReleased = event => if(isPrimaryButton(event)) endSelection(event)
  }

  private def isPrimaryButton(event: MouseEvent): Boolean = event.getButton == MouseButton.PRIMARY
}

object NetworkRenderingPanel {
  def apply(): NetworkRenderingPanel = new NetworkRenderingPanel()
}