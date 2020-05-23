package testing

import akka.actor.AbstractActor.Receive
import akka.actor.{Actor, ActorSystem, Props, typed}

import scala.language.postfixOps
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.util.Random


class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {


  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._

  "A simple actor" should {
    "send back message " in {
      val echoActor = system.actorOf(Props[NotWorkingActor])
      val msg = "hello"
      echoActor ! msg
      expectMsg(msg)
      //testActor is used
    }

  }

  "notResponding actor " should {
    "not respond" in {
      val actor = system.actorOf(Props[NotRespondingActor])
      val msg = "are you there?"
      actor ! msg
      expectNoMessage(1 second)
    }
  }

  "labTestActor" should {
    val actor = system.actorOf(Props[LabTestActor])
    "turn to upper case" in {
      val originalMessage = "someLowerSomeNOT"
      actor ! originalMessage
      val reply = expectMsgType[String]
      assert(reply == "SOMELOWERSOMENOT")
    }
    "return hi or hello" in {
      actor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }
    "reply with favorite Tech" in {
      actor ! "favoriteTech"
      expectMsgAllOf("Scala", "Akka")
    }

    "reply with cool tech in different way" in {
      actor ! "favoriteTech"
      val messages = receiveN(2)
    }

    "reply with cool tech in a fancy way" in {
      actor ! "favoriteTech"
      expectMsgPF() {
        case "Scala" =>
        case "Akka" =>
      }
    }

  }


}

object BasicSpec {

  class NotWorkingActor extends Actor {
    override def receive: Receive = {
      case message: String => sender() ! message
    }
  }

  class NotRespondingActor extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val random = new Random()

    override def receive: Receive = {
      case "greeting" => if (random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      case "favoriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase()
    }
  }

}
