package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App{
  class SimpleActor(val name: String) extends Actor {

    override def receive: Receive = {
      case "Hi" => context.sender() ! s" $name says Hello there back "
      case message : String => println(s"$name received $message")
      case number: Int => println(number+43)
      case SayHiTo(ref) => ref ! "Hi"
    }
  }
  object SimpleActor {
    def props(name: String) = Props(new SimpleActor(name))
  }

val system = ActorSystem("actorCapabilitiesDemo")
  val alice = system.actorOf(SimpleActor.props( "alice"),"alice")
  val bob = system.actorOf(SimpleActor.props("bob"),"bob")
  val mark = system.actorOf(SimpleActor.props("mark"),"mark")
  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)
  mark ! SayHiTo(bob)



  //1 messages can be of any type
  //1st condition  - messages must be immutable
  //2nd condition - messages must be serializable
  //case classes and case objects are serializable by default

  //2 actors have information about their context and reference to itself ( context.self)
  // context.self === this

  //3 actors can reply to messages

  //4 dead letter, if message is sent to noone(for instance we reply to the main context)
  // the message is sent to dead letters
  //5 forwarding the message, the original sender is kept



}
