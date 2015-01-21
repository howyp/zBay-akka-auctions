import java.util.concurrent.TimeUnit

import akka.actor.{TypedActor, ActorRef, Props}
import akka.pattern._
import akka.routing.FromConfig
import akka.util.Timeout
import org.joda.time.DateTime

import scala.math.BigDecimal

class zBayTypedActor extends zBay with TypedActor.PreStart {
  import API.Protocol._

  var apiActor: ActorRef = _
  var auctionIds = Stream.from(1).iterator
  var liveAuctions = Set[Long]()

  implicit val timeout = Timeout(10, TimeUnit.SECONDS)
  implicit val ec = TypedActor.context.dispatcher

  override def preStart() {
    apiActor = TypedActor.context.actorOf(Props[API].withRouter(FromConfig()), "api")
  }

  def createAuction(endTime: DateTime) = {
    val auctionId = auctionIds.next()
    TypedActor.context.actorOf(Props(new Auction(endTime)), "auction" + auctionId)
    liveAuctions = liveAuctions + auctionId
    auctionId.toLong
  }

  def placeBid(auctionId: Long, userId: Long, value: BigDecimal) =
    (apiActor ? BidRequest(auctionId, userId, value)).mapTo[BidStatus]

  def status(auctionId: Long) =
    (apiActor ? StatusRequest(auctionId)).mapTo[AuctionValue]

  def find(endTime: DateTime) =
    (apiActor ? QueryRequest(endTime, liveAuctions)).map { case QueryResponse(ids) => ids }
}
