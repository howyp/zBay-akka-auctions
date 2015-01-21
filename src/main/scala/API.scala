import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.pattern._
import akka.util.Timeout
import org.joda.time.DateTime

import scala.concurrent.Future

class API extends Actor {
  import Auction.Protocol._
  import API.Protocol._
  import zBay.Protocol._

  implicit val timeout = Timeout(10, TimeUnit.SECONDS)
  implicit val ec = context.dispatcher

  def receive = {
    case AuctionBidRequest(auctionId, userId, value) =>
      auctionActorFor(auctionId).tell(Bid(value, userActorFor(userId)), sender)

    case AuctionStatusRequest(auctionId) =>
      auctionActorFor(auctionId).tell(StatusRequest, sender)

    case QueryRequest(expectedEndTime, currentAuctionIds) =>
      val auctionIds: Set[Future[Option[Long]]] = currentAuctionIds.map { auctionId =>
        (auctionActorFor(auctionId) ? DetailsRequest).map {
          case DetailsResponse(t) => if (t == expectedEndTime) Some(auctionId) else None
        }
      }
      Future.sequence(auctionIds).map(_.flatten).map(QueryResponse(_)).pipeTo(sender)
  }

  def userActorFor(userId: Long) = context.actorFor(s"../../user$userId")
  def auctionActorFor(auctionId: Long) = context.actorSelection(s"../../auction$auctionId")
}
object API {
  object Protocol {
    case class QueryRequest(endTime: DateTime, currentAuctions: Set[Long])
    case class QueryResponse(matchingAuctions: Set[Long])
  }
}