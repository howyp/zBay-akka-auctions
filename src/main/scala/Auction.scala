import akka.actor.Actor
import org.joda.time.{Interval, DateTime}
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import DateTime.now

class Auction(endTime: DateTime) extends Actor {
  import Auction.Protocol._

  var currentHighestBid = BigDecimal(0)
  var currentStatus: State = Running

  context.system.scheduler.scheduleOnce(atEndTime(), self, EndNotification)(context.system.dispatcher)

  def receive = {
    case StatusRequest   => sender ! StatusResponse(currentHighestBid, currentStatus)
    case Bid(value)      => currentHighestBid = currentHighestBid max value
    case EndNotification => currentStatus = Ended
  }

  def atEndTime() = FiniteDuration(new Interval(now, endTime).toDurationMillis, TimeUnit.MILLISECONDS)
}

object Auction {
  object Protocol {
    case object StatusRequest
    case class StatusResponse(currentHighestBid: BigDecimal, state: State)
    case class Bid(value: BigDecimal)
    case object EndNotification

    sealed trait State
    case object Running extends State
    case object Ended extends State
  }
}