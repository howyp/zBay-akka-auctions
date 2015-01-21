import akka.actor.{FSM, ActorRef, Actor}
import org.joda.time.{Interval, DateTime}
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import DateTime.now

import scala.math.BigDecimal

class Auction(endTime: DateTime) extends Actor with FSM[Auction.Lifecycle, AuctionValue] {
  import Auction.Protocol._
  import Auction.Lifecycle._
  import AuctionValue._
  import User.Protocol.BidOnNotification

  context.system.scheduler.scheduleOnce(atEndTime, self, EndNotification)(context.system.dispatcher)

  startWith(New, NotSold)

  when(New) {
    case Event(Bid(value, from), NotSold) =>
      from ! BidOnNotification(self)
      goto (Active) using Sold(value) replying BidStatus.Accepted
  }

  when(Active) {
    case Event(Bid(value, from), Sold(highestBid)) =>
      from ! BidOnNotification(self)
      stay using Sold(highestBid max value) replying BidStatus.Accepted
  }

  when(Closed) {
    case Event(Bid(_, _), _) =>
      stay replying BidStatus.Rejected
  }

  whenUnhandled {
    case Event(DetailsRequest,   _)  => stay replying DetailsResponse(endTime)
    case Event(EndNotification,  _)  => goto (Closed)
    case Event(StatusRequest, value) => stay replying value
  }

  def atEndTime = FiniteDuration(new Interval(now, endTime).toDurationMillis, TimeUnit.MILLISECONDS)
}

object Auction {
  object Protocol {
    case object StatusRequest
    case class StatusResponse(currentHighestBid: BigDecimal, state: AuctionValue)

    case object DetailsRequest
    case class DetailsResponse(endTime: DateTime)

    case class Bid(value: BigDecimal, from: ActorRef)

    case object EndNotification
  }

  trait Lifecycle
  object Lifecycle {
    case object New extends Lifecycle
    case object Active extends Lifecycle
    case object Closed extends Lifecycle
  }
}