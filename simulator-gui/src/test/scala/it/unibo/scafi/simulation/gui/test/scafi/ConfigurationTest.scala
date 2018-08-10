package it.unibo.scafi.simulation.gui.test.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiSeed, ScafiWorldInitializer, scafiWorld}
import org.scalatest.{FunSpec, Matchers}

class ConfigurationTest extends FunSpec with Matchers{
  val checkThat = new ItWord

  val world = scafiWorld
  val node = 100
  val width = 10
  val space = 1
  val height = 10
  val aNodeId = 1
  checkThat("random initializer create a random world") {
    world.clear()
    val init = ScafiWorldInitializer.Random(node,width,height).init(ScafiSeed.standard)
    assert(world.nodes.nonEmpty)
    assert(world.nodes.size == node)
    assert(world(aNodeId).get.devices.nonEmpty)
    assert(world.boundary.isEmpty)
  }

  checkThat("grid initializer create a grid like workd") {
    world.clear()
    val init = ScafiWorldInitializer.Grid(width,height,space).init(ScafiSeed.standard)
    assert(world.nodes.nonEmpty)
    assert(world.nodes.size == width * height)
    assert(world(aNodeId).get.devices.nonEmpty)
    assert(world.boundary.isEmpty)
  }
}
