import akka.actor.Actor

class Auction extends Actor {
  import Auction.Protocol._

  var currentHighestBid = BigDecimal(0)

  def receive = {
    case StatusRequest => sender ! StatusResponse(currentHighestBid)
    case Bid(value)    => currentHighestBid = currentHighestBid max value
  }
}
object Auction {
  object Protocol {
    case object StatusRequest
    case class StatusResponse(currentHighestBid: BigDecimal)
    case class Bid(value: BigDecimal)
  }
}

