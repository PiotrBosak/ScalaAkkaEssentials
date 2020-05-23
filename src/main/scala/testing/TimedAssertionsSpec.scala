package testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import testing.TimedAssertionsSpec.{WorkResult, WorkerActor}

import scala.concurrent.duration._
import scala.util.Random

class TimedAssertionsSpec extends TestKit(ActorSystem("TimedAssertionSpec",
  ConfigFactory.load().getConfig("specialTimedAssertionsConfig")))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "A worker actor" should {
    val workerActor = system.actorOf(Props[WorkerActor])
    "reply with a number in a timely manner" in {
      within(300 millis, 1 second) {
        workerActor ! "work"
        expectMsg(WorkResult(5))
      }
    }
    "reply with valid work at a reasonable speed" in {
      within(500 millis) {
        workerActor ! "workSequence"
        val results = receiveWhile[Int](max = 2 seconds, idle = 500 millis, messages = 10) {
          case WorkResult(result) => result
        }
        assert(results.sum == 10)
      }

    }
    "reply to testProbe in time block" in {
      within(1 second) {
        val probe = TestProbe()
        probe.send(workerActor, "work")
        probe.expectMsg(WorkResult(5))// not gonna work because timeout is 0.3 in application.config
      }
    }
  }
}

object TimedAssertionsSpec {

  case class WorkResult(result: Int)

  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "work" =>
        //long computation
        Thread.sleep(500)
        sender() ! WorkResult(5)
      case "workSequence" =>
        val random = new Random()
        for (_ <- 1 to 10) {
          Thread.sleep(random.nextInt(50))
          sender() ! WorkResult(1)
        }
    }
  }

}
