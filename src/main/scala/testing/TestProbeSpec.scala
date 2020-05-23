package testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class TestProbeSpec extends TestKit(ActorSystem("TestProbSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TestProbeSpec._

  "A master actor" should {

    "register a slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
    }
    "send the work to the slave actor" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
      val workLoad = "I love akka"
      master ! Work(workLoad)
      //the interaction between the master and the slave
      slave.expectMsg(SlaveWork(workLoad, testActor))
      slave.reply(WorkCompleted(3, testActor))
      expectMsg(Report(3)) //test actor receives report(3)
    }
    "if sent 2 works, send 2 reports back" in {
      //      val master = system.actorOf(Props[Master])
      //      val slave = TestProbe("slave")
      //      master ! Register(slave.ref)
      //      expectMsg(RegistrationAck)
      //      master ! Work("Scala is really damn dope")
      //      slave.expectMsg(SlaveWork("Scala is really damn dope",testActor))
      //      slave.reply(WorkCompleted(5,testActor))
      //      master ! Work("Scala is really damn dope again")
      //      slave.expectMsg(SlaveWork("Scala is really damn dope again",testActor))
      //      slave.reply(WorkCompleted(6,testActor))
      //      expectMsg(Report(5))
      //      expectMsg(Report(5+6)) works but not the best way of doing it
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
      val workLoad = "Scala is really damn dope"
      master ! Work(workLoad)
      master ! Work(workLoad)
      slave.receiveWhile() {
        case SlaveWork(`workLoad`, `testActor`) => slave.reply(WorkCompleted(5, testActor))
      }
      expectMsg(Report(5))
      expectMsg(Report(10))


    }
  }
}

object TestProbeSpec {

  //scenario
  /*
  word counting actor hierarchy master-slave
  1.send some work to the master
  2.master sends slave piece of work
  3, slave processes and  responses
  4. master aggregates the result
  5.master sends the result back

   */
  case class Report(total: Int)

  case class WorkCompleted(count: Int, originalRequester: ActorRef)

  case class Work(text: String)

  case class Register(slaveRef: ActorRef)

  case class SlaveWork(text: String, originalRequester: ActorRef)

  case object RegistrationAck

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) =>
        context.become(online(slaveRef, 0))
        sender() ! RegistrationAck
      case _ =>
    }

    def online(slaveRef: ActorRef, value: Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(count, originalRequester) =>
        val newTotal = value + count
        originalRequester ! Report(newTotal)
        context.become(online(slaveRef, newTotal))
    }
  }

  //class slave ....

}
