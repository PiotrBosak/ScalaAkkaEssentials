package playground

import akka.actor.ActorSystem

object ScalaPlayground extends App{

  val actorSystem = ActorSystem("Helloakka")
  println(actorSystem.name)

  trait Receiver[T] {
    def apply(): T
  }





}
