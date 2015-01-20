import akka.actor.{ActorRef, Props, Actor}
import org.joda.time.DateTime

class zBay extends Actor {
  import zBay.Protocol._

  var auctionIds = Stream.from(1).iterator
  var apiActor: ActorRef = _

  override def preStart() {
    apiActor = context.actorOf(Props[API])
  }

  def receive = {
    case AuctionCreateRequest(endTime) => createAuction(endTime)
    case stsRq: AuctionStatusRequest   => apiActor.forward(stsRq)
    case bidRq: AuctionBidRequest      => apiActor.forward(bidRq)
  }

  def createAuction(endTime: DateTime) = {
    val auctionId = auctionIds.next()
    context.actorOf(Props(new Auction(endTime)), "auction" + auctionId)
    sender ! auctionId.toLong
  }
}
object zBay {
  object Protocol {
    case class AuctionCreateRequest(endTime: DateTime)
    case class AuctionStatusRequest(auctionId: Long)
    case class AuctionBidRequest(auctionId: Long,
                                 userId: Long,
                                 value: BigDecimal)
  }
}

