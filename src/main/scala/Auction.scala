import akka.actor.{FSM, ActorRef, Actor}
import org.joda.time.{Interval, DateTime}
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import DateTime.now

import scala.math.BigDecimal

class Auction(endTime: DateTime) extends Actor with FSM[Auction.State, BigDecimal] {
  import Auction.Protocol._
  import Auction._
  import User.Protocol.BidOnNotification

  context.system.scheduler.scheduleOnce(atEndTime, self, EndNotification)(context.system.dispatcher)

  val NoBid = BigDecimal(0)
  startWith(Running, NoBid)

  when(Running) { respondToStatusRequestAs(Running) orElse {
    case Event(Bid(value, from), highestBid) => from ! BidOnNotification(self)
                                                stay using (highestBid max value) replying BidAccepted
    case Event(EndNotification,  NoBid)      => goto (NotSold)
    case Event(EndNotification,  _)          => goto (Sold)
    case Event(DetailsRequest,   _)          => stay replying DetailsResponse(endTime)
  }}

  when(Sold){ respondToStatusRequestAs(Sold) }
  when(NotSold){ respondToStatusRequestAs(NotSold) }

  def respondToStatusRequestAs(state: Auction.State): StateFunction = {
    case Event(StatusRequest, winningBid) => stay replying StatusResponse(winningBid, state)
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
  }

  sealed trait State
  case object Running extends State
  case object Sold extends State
  case object NotSold extends State
}