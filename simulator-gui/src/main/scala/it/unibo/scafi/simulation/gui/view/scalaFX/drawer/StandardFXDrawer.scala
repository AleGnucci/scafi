package it.unibo.scafi.simulation.gui.view.scalaFX.drawer

import javafx.scene.control.Label
import javafx.scene.shape.Shape

import it.unibo.scafi.simulation.gui.launcher.SensorName._
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Polygon => InternalPolygon, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.model.simulation.BasicSensors.{DisplaySensor, OnOffSensor}
import it.unibo.scafi.simulation.gui.view.Drawer
import it.unibo.scafi.simulation.gui.view.scalaFX._

import scalafx.application.Platform
import scalafx.geometry.{Point2D => FXPoint}
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Ellipse, Rectangle}

object StandardFXDrawer extends FXDrawer {

  override type OUTPUTNODE = javafx.scene.Node
  val colors: Map[String, Color] = Map(sens1.name -> Color.Red,
    sens2.name -> Color.Yellow,
    sens3.name -> Color.Blue,
    gsensor.name -> Color.LimeGreen)
  val size: Map[String, Double] = Map(sens1.name -> 1, sens2.name -> 3, sens3.name -> 5, gsensor.name -> 7)
  val radius = 2

  override def nodeGraphicsNode[INPUTNODE <: World#NODE](node: INPUTNODE): OUTPUTNODE = nodeToShape.create(node)


  def deviceToGraphicsNode[INPUTDEV <: World#DEVICE](node: OUTPUTNODE, dev: INPUTDEV, lastValue: Option[OUTPUTNODE]): Option[OUTPUTNODE] = {
    if (lastValue.isDefined) {
      Platform.runLater {
        val lastDev = lastValue.get
        dev match {
          case DisplaySensor(value) => {
            lastDev.asInstanceOf[Label].setText(value)
          }
          case OnOffSensor(value) => {
            lastDev.setVisible(value)
          }
          case _ =>
        }
      }
      None
    } else {
      drawNode(dev,node)
    }
  }

  private def drawNode[INPUTDEV <: World#DEVICE](dev: INPUTDEV, node: OUTPUTNODE): Option[OUTPUTNODE] = {
    import scalafx.Includes._
    val point = nodeToAbsolutePosition(node)
    dev match {
      case DisplaySensor(value) => {
        val label = new Label(value)
        label.layoutX.bind(node.translateX + point.x)
        label.layoutY.bind(node.translateY + point.y)
        Some(label)
      }
      case OnOffSensor(value) => {
        val res : Node = new Ellipse {
          this.centerX.bind(node.translateX + point.x)
          this.centerY.bind(node.translateY + point.y)
          this.radiusX = size(dev.name.toString)
          this.radiusY = size(dev.name.toString)
          this.strokeWidth = radius
          val color: Color = colors(dev.name.toString)
          this.stroke = color
          this.fill = Color.Transparent
          this.visible = value
          this.smooth = false
        }
        Some(res)
      }
      case _ => None
    }
  }
}
