import akka.actor.{ActorRef, Actor}
import org.joda.time.{Interval, DateTime}
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import DateTime.now

class Auction(endTime: DateTime) extends Actor {
  import Auction.Protocol._
  import User.Protocol.BidOnNotification

  var currentHighestBid = BigDecimal(0)
  var currentStatus: State = Running

  context.system.scheduler.scheduleOnce(atEndTime, self, EndNotification)(context.system.dispatcher)

  def receive = runningBehaviour

  val runningBehaviour: Receive = {
    case StatusRequest    => sender ! StatusResponse(currentHighestBid, Running)
    case Bid(value, bidder) => currentHighestBid = currentHighestBid max value
                               bidder ! BidOnNotification(self)
                               sender ! BidAccepted
    case EndNotification  => context.become(endedBehaviour)
    case DetailsRequest   => sender ! DetailsResponse(endTime)
  }

  val endedBehaviour: Receive = {
    case StatusRequest   => sender ! StatusResponse(currentHighestBid, Ended)
  }

  def atEndTime = FiniteDuration(new Interval(now, endTime).toDurationMillis, TimeUnit.MILLISECONDS)
}

object Auction {
  object Protocol {
    case object StatusRequest
    case class StatusResponse(currentHighestBid: BigDecimal, state: State)

    case object DetailsRequest
    case class DetailsResponse(endTime: DateTime)

    case class Bid(value: BigDecimal, from: ActorRef)
    case object BidAccepted

    case object EndNotification

    sealed trait State
    case object Running extends State
    case object Ended extends State
  }
}