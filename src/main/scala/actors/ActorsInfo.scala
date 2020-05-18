package actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsInfo extends App{
  //part 1 - actor system
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)
  //part 2 create actors
  //word count actor

  class WordCountActor extends Actor {
    //internal data
    var totalWords = 0
    //behaviour
    def receive: PartialFunction[Any,Unit] = { // pf[any, unit] is aliased as Receive
      case message: String =>
        totalWords += message.split(" ").length
        println(totalWords)

      case msg => println(s"I can't understand ${msg.toString}")
    }
  }
  //part 3 instantiate our actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor],"wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor],"anotherWordCounter")

  //part 4 communicate
  wordCounter ! "I am learning akka" // ! method is known as tell
  anotherWordCounter ! "I am learning akka too"
  // it's async


  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => "something"
    }
  }
  val person = actorSystem.actorOf(Props(new Person("Bob")))//not to soog
  val betterPerson = actorSystem.actorOf(Person.props("Mark"))//better
   object Person {
    def props(name: String) = Props(new Person(name))// that's better
  }

}
