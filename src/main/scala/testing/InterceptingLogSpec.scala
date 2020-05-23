package testing

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import testing.InterceptingLogSpec.CheckoutActor

class InterceptingLogSpec extends TestKit(ActorSystem("interceptingLogs",ConfigFactory.load().getConfig("interceptingLogMessages")))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {
  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import InterceptingLogSpec._
  "A checkout flow" should {
    "correctly log the dispatch of an order" in {
      EventFilter.info(s"order is confirmed", occurrences = 1) intercept // we can use regex if needed
       {
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout("some iterm","1111")
      }
    }
  }
}


object InterceptingLogSpec {

  case class Checkout(item: String, creditCard: String)

  case class AuthorizeCard(card: String)

  case object PaymentAccepted

  case object PaymentDenied

  case class DispatchOrder(item: String)

  case object OrderConfirmed

  case object OrderFailed

  class CheckoutActor extends Actor {
    private val paymentManager = context.actorOf(Props[PaymentManager])
    private val fulfillmentManager = context.actorOf(Props[FulfillmentManager])

    override def receive: Receive = awaitingCheckout

    def awaitingCheckout: Receive = {
      case Checkout(item, card) =>
        paymentManager ! AuthorizeCard(card)
        context.become(pendingPayment(item))
    }

    def pendingPayment(item: String): Receive = {
      case PaymentAccepted =>
        fulfillmentManager ! DispatchOrder(item)
        context.become(pendingFulfillment(item))
      case PaymentDenied => //todo do something

    }

    def pendingFulfillment(item: String): Receive = {
      case OrderConfirmed => context.become(awaitingCheckout)
    }
  }

  class PaymentManager extends Actor  {
    override def receive: Receive = {
      case AuthorizeCard(card) =>
        if (card.isBlank) sender() ! PaymentDenied // some  dummy validation
        else sender() ! PaymentAccepted
    }
  }

  class FulfillmentManager extends Actor with ActorLogging {
    override def receive: Receive = {
      case DispatchOrder(item) =>
        log.info(s"order is confirmed")
        sender() ! OrderConfirmed //again, some dummy implementation, check in db or sth


    }
  }

}
