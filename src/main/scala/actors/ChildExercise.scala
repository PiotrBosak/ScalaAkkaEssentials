package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildExercise extends App {

  object WordCounterMaster {

    case class Initialize(nChildren: Int)

    case class WordCountTask(id: Int, text: String)

    case class WordCountReply(id: Int, count: Int)

  }

  //distribute word counting
  class WordCounterMaster extends Actor {

    import WordCounterMaster._

    def withChildren(children: Seq[ActorRef], currentIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        val originalSender = sender()
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        val newTaskId = currentTaskId + 1
        if (currentIndex == children.size)
          context.become(withChildren(children, 0, newTaskId, newRequestMap))
        else {
          children(currentIndex) ! WordCountTask(currentTaskId, text)
          context.become(withChildren(children, currentIndex + 1, newTaskId, newRequestMap))
        }
      case WordCountReply(id, count) =>
        val originalSender = requestMap(id)
        originalSender ! count
       context.become(withChildren(children, currentIndex, currentTaskId, requestMap - id))
    }

    override def receive: Receive = {
      case Initialize(n) =>
        val children = for (i <- 0 until n) yield context.actorOf(WordCounterWorker.props, s"child$i")
        context.become(withChildren(children, 0, 0, Map()))
    }
  }

  class WordCounterWorker extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        val count = text.split(" ").length
        sender() ! WordCountReply(id, count)
    }
  }

  object WordCounterWorker {
    def props: Props = Props(new WordCounterWorker)
  }

  /*
  create master
  initialize(10)
  send
   */

  import WordCounterMaster._

  val system = ActorSystem("system")
  val master = system.actorOf(Props[WordCounterMaster])
}
