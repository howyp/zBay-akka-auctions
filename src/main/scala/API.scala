import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.pattern._
import akka.util.Timeout
import org.joda.time.DateTime

import scala.concurrent.Future

class API extends Actor {
  import Auction.Protocol.{Bid, DetailsRequest, DetailsResponse}
  import API.Protocol._

  implicit val timeout = Timeout(10, TimeUnit.SECONDS)
  implicit val ec = context.dispatcher

  def receive = {
    case BidRequest(auctionId, userId, value) =>
      auctionActorFor(auctionId).tell(Bid(value, userActorFor(userId)), sender)

    case StatusRequest(auctionId) =>
      auctionActorFor(auctionId).tell(Auction.Protocol.StatusRequest, sender)

    case QueryRequest(expectedEndTime, currentAuctionIds) =>
      val auctionIds = currentAuctionIds.map { auctionId =>
        (auctionActorFor(auctionId) ? DetailsRequest).map {
          case DetailsResponse(t) => if (t == expectedEndTime) Some(auctionId) else None
        }
      }
      Future.sequence(auctionIds).map(_.flatten).map(QueryResponse.apply).pipeTo(sender)
  }

  def userActorFor(userId: UserId) = context.actorFor(s"../../user$userId")
  def auctionActorFor(auctionId: AuctionId) = context.actorSelection(s"../../auction$auctionId")
}
object API {
  object Protocol {
    case class BidRequest(auctionId: AuctionId, userId: UserId, value: AuctionValue.Price)
    case class StatusRequest(auctionId: AuctionId)
    case class QueryRequest(endTime: DateTime, currentAuctions: Set[AuctionId])
    case class QueryResponse(matchingAuctions: Set[AuctionId])
  }
}