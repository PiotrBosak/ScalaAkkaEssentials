package actors

import actors.ChildActors.Parent.{CreateChild, TellChild}
import akka.actor.{Actor, ActorRef, Props}

object ChildActors extends App {

  //actors can create other actors
  class Parent extends Actor {

    def beforeChild():Receive = {
      case CreateChild(name) =>
        val child = context.actorOf(Child.props(name),name)
        context.become(withChild(child))
    }

    def withChild(child: ActorRef): Receive = {
      case TellChild(message) => if (child != null) child forward  message
    }

    override def receive: Receive = beforeChild()
  }

  object Parent {

    case class CreateChild(name: String)

    case class TellChild(message: String)

  }

  class Child(val name:String) extends Actor {
    override def receive: Receive = {
      case message => println("I'm a child")
    }
  }

  object Child {
    def props(name:String) = Props(new Child(name))
  }


}
