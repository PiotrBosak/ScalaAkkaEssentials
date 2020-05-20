package actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object AkkaConfig extends App{

  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case m => log.info(m.toString)
    }
  }

  /**
   * inline config
   */
  val configString =
    """
      |akka {
      | loglevel = "ERROR"
      | }
      |""".stripMargin

  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("system",ConfigFactory.load(config))
  val actor = system.actorOf(Props[SimpleLoggingActor])
  actor ! "plumbiz"

  val system2 = ActorSystem("anotherSystem")
  val defaultConfig = system2.actorOf(Props[SimpleLoggingActor])
  defaultConfig ! "gowno"
  //METHOD 3 SEPERATE CONFIG IN THE SAME FILE
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("special",specialConfig)
  val actor2 = specialConfigSystem.actorOf(Props[SimpleLoggingActor])

  actor2 ! "something "

  /**
   *
   */
  val seperateConfig = ConfigFactory.load("secret/secret.conf")
  println(seperateConfig.getString("akka.logLevel"))
  //we can use properties or json
  val configJson = ConfigFactory.load("json/jsonConfig.json")
  println(configJson.getString("logLevel"))
}
