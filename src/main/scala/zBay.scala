import akka.actor.{ActorRef, Props, Actor}
import akka.routing.FromConfig
import org.joda.time.DateTime

class zBay extends Actor {
  import zBay.Protocol._
  import API.Protocol._

  var auctionIds = Stream.from(1).iterator
  var apiActor: ActorRef = _
  var liveAuctions = Set[Long]()

  override def preStart() {
    apiActor = context.actorOf(Props[API].withRouter(FromConfig()), "api")
  }

  def receive = {
    case AuctionCreateRequest(endTime)  => createAuction(endTime)
    case rq@ ( _: AuctionBidRequest
             | _: AuctionStatusRequest) => apiActor.forward(rq)
    case AuctionQueryRequest(endTime)   => apiActor.tell(Query(endTime, liveAuctions), sender)
  }

  def createAuction(endTime: DateTime) = {
    val auctionId = auctionIds.next()
    context.actorOf(Props(new Auction(endTime)), "auction" + auctionId)
    liveAuctions = liveAuctions + auctionId
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
    case class AuctionQueryRequest(endTime: DateTime)
    case class AuctionQueryResponse(ids: Set[Long])
  }
}

