package actors

import akka.actor.{Actor, ActorSystem}

object ActorCapabilities extends App{
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message : String => println(s"I've received $message")
    }
  }

val system = ActorSystem("actorCapabilitiesDemo")

}
