package actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}

object ActorLogging extends App {

  val system = ActorSystem("system")

  //explicit logging
  class Logger extends Actor {
    val logger: LoggingAdapter = Logging(context.system, this)

    override def receive: Receive = {
      /*
      1 debug
      2info
      3 warning
      4 error
       */
      case message => logger.info(message.toString)
    }
  }

  val logger = system.actorOf(Props[Logger])
  logger ! "plumba"

  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      case (a,b) => log.info("Two parameters ${} and {}",a,b)
      case message => log.info("something")
    }
  }

}
