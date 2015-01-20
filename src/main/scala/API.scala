import akka.actor.Actor
import Auction.Protocol.Bid

class API extends Actor {
  import Auction.Protocol._
  import zBay.Protocol._

  def receive = {
    case AuctionBidRequest(auctionId, userId, value) =>
      auctionActorFor(auctionId).tell(Bid(value, userActorFor(userId)), sender)

    case AuctionStatusRequest(auctionId) =>
      auctionActorFor(auctionId).tell(StatusRequest, sender)
  }

  def userActorFor(userId: Long) = context.actorFor(s"../../user$userId")
  def auctionActorFor(auctionId: Long) = context.actorSelection(s"../../auction$auctionId")
}