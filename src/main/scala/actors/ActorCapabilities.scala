package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor(val name: String) extends Actor {

    override def receive: Receive = {
      case "Hi" => context.sender() ! s" $name says Hello there back "
      case message: String => println(s"$name received $message")
      case number: Int => println(number + 43)
      case SayHiTo(ref) => ref ! "Hi"
    }
  }

  object SimpleActor {
    def props(name: String) = Props(new SimpleActor(name))
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val alice = system.actorOf(SimpleActor.props("alice"), "alice")
  val bob = system.actorOf(SimpleActor.props("bob"), "bob")
  val mark = system.actorOf(SimpleActor.props("mark"), "mark")

  case class SayHiTo(ref: ActorRef)

  //  alice ! SayHiTo(bob)
  //  mark ! SayHiTo(bob)


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


  /**
   * counter actor
   * -increment messages
   * decrement message
   *
   * 2. bank account as an actor
   * receives
   * -deposit
   * -withdraw an amount
   * -statement
   * replies with
   * -success or failure
   *
   * interact with some other kind of actor that
   */

  object BankAccount {

    case class DepositMessage(amount: Int)

    case class WithdrawMessage(amount: Int)

    case object StatementMessage

    case class TransactionSuccess(message: String)

    case class TransactionFailure(message: String)


  }

  class BankAccount extends Actor {

    import BankAccount._

    var balance = 0

    override def receive: Receive = {
      case DepositMessage(amount) =>
        if (amount < 0) sender() ! TransactionFailure("incorrect amount")
        else {
          balance += amount
          sender() ! TransactionSuccess("successful deposit")
        }
      case WithdrawMessage(amount) =>
        if (amount < 0) sender() ! TransactionFailure("incorrect amount")
        else if (amount > balance) sender() ! TransactionFailure("insufficient funds")
        else {
          balance -= amount
          sender() ! TransactionSuccess("successful withdraw")
        }
      case StatementMessage => sender() ! s"current balance is $balance"
    }

  }

  class InterActor(val bankAccount: ActorRef) extends Actor {

    import BankAccount._

    override def receive: Receive = {
      case SomeMessage =>
        bankAccount ! DepositMessage(5999)
        bankAccount ! WithdrawMessage(54435)
        bankAccount ! StatementMessage
      case any => println(s"${any.toString}")
    }
  }


  case object SomeMessage


  class IncrementMessage

  class DecrementMessage

  class PrintMessage

  class CounterActor extends Actor {
    var x = 0

    override def receive: Receive = {
      case i: IncrementMessage => x += 1
      case d: DecrementMessage => x -= 1
      case p: PrintMessage => println(x)
    }

  }
  object InterActor {
    def props(account: ActorRef): Props = Props(new InterActor(account))
  }
val bankAccount = system.actorOf(Props[BankAccount],"account")
  val actor = system.actorOf(InterActor.props(bankAccount),"actor")
  actor ! SomeMessage


}
