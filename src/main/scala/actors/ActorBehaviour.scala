package actors

import actors.ActorBehaviour.Counter.{Decrement, Increment, Print}
import actors.ActorBehaviour.Mom.{Food, StartInteraction}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorBehaviour extends App {

  //

  object FussyKid {

    case object Accept

    case object Reject

    val HAPPY = "happy"
    val SAD = "sad"
  }

  class FussyKid extends Actor {

    import FussyKid._
    import Mom._

    var state: String = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(message) =>
        if (state == HAPPY) sender() ! Accept
        else if (state == SAD) sender() ! Reject
        else throw new Exception("should never happen")
    }
  }

  object Mom {
    def props(kid: ActorRef): Props = Props(new Mom(kid))

    case class Food(food: String)

    case class Ask(message: String)

    case object StartInteraction

    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

  class Mom(val kid: ActorRef) extends Actor {

    import Mom._
    import FussyKid._

    override def receive: Receive = {
      case StartInteraction =>
        kid ! Food(VEGETABLE)
        kid ! Food(VEGETABLE)
        kid ! Food(VEGETABLE)
        kid ! Food(CHOCOLATE)
        kid ! Food(CHOCOLATE)
        kid ! Ask("wanna play now?")
      case Accept => println("my kid wants to play with me")
      case Reject => println("my kid doesn't want to play with me")
    }
  }

  val system = ActorSystem("system")

  //  val kiddo = system.actorOf(Props[FussyKid],"kiddo")
  //  val mommy = system.actorOf(Mom.props(kiddo),"mommy")
  //  mommy ! StartInteraction
  //  //that's bad, we use vars

  class StatelessFussyKid extends Actor {

    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)
      case Food(CHOCOLATE) => context.become(happyReceive, discardOld = false)
      case Ask(_) => sender() ! Accept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, discardOld = false)
      case Food(CHOCOLATE) => context.unbecome()
      case Ask(_) => sender() ! Reject
    }
  }

  val statelessKid = system.actorOf(Props[StatelessFussyKid], "stateless")
  val mommy = system.actorOf(Mom.props(statelessKid), "mommy")
  mommy ! StartInteraction

  /**
   * 1 - recreate the counter actor with context become and no mutable state
   * 2- simplified voting system,
   *
   */
  object Counter {

    case object Increment

    case object Decrement

    case object Print

  }

  class Counter extends Actor {
    override def receive: Receive = aReceive(0)

    def aReceive(current: Int): Receive = {
      case Increment => context.become(aReceive(current + 1))
      case Decrement => context.become(aReceive(current - 1))
      case Print => print(current)
    }


  }

  val counter = system.actorOf(Props[Counter], "counter")

  //  counter ! Print
  //  counter ! Increment
  //  counter ! Increment
  //  counter ! Increment
  //  counter ! Increment
  //  counter ! Decrement
  //  //  counter ! Decrement
  //  //  counter ! Decrement
  //  counter ! Print

  case class Vote(candidate: String)

  class Citizen extends Actor {
    override def receive: Receive = beforeVote()
    def beforeVote(): Receive = {
      case Vote(candidate) => context.become(afterVote(candidate))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def afterVote(candidate: String): Receive = {
      case Vote(_) => println("I can't change my mind now")
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case object VoteStatusRequest

  case class VoteStatusReply(candidate: Option[String])

  case class AggregateVotes(citizens: Set[ActorRef])

  case object PrintResults

  class VoteAggregator extends Actor {
    val map: collection.mutable.Map[String, Int] = collection.mutable.Map()

    def addToMap(candidate: Option[String]): Unit = {
      if (candidate.isEmpty) ()
      else {
        println("adding to map")
        val s = candidate.get
        if (map.contains(s)) map(s) += 1
        else map.addOne((s, 1))
      }
    }

    override def receive: Receive = {
      case AggregateVotes(citizens) => citizens.foreach(a => a ! VoteStatusRequest)
      case VoteStatusReply(candidate) => addToMap(candidate)
      case PrintResults => println(map.toString)
    }
  }

  val alice = system.actorOf(Props[Citizen],"alice")
  val mark = system.actorOf(Props[Citizen],"mark")
  val bob = system.actorOf(Props[Citizen],"bob")
  val john = system.actorOf(Props[Citizen],"john")
  val peter = system.actorOf(Props[Citizen],"peter")
  alice ! Vote("a")
  mark ! Vote("a")
  bob ! Vote("b")
  john ! Vote("c")

  val voteAggregator = system.actorOf(Props[VoteAggregator],"aggregate")
  voteAggregator ! AggregateVotes(Set(alice, mark, bob, john, peter))
  Thread.sleep(1000)
  voteAggregator ! PrintResults


}
