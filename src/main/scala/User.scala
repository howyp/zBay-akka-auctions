import akka.actor.{ActorRef, Actor}

class User extends Actor {
  import User.Protocol._

  var auctions = List[ActorRef]()

  def receive = {
    case BidOnNotification(auction)   => auctions = auction :: auctions
    case ListAuctionsRequest          => sender ! ListAuctionsResponse(auctions)
  }
}
object User {
  object Protocol {
    case object ListAuctionsRequest
    case class ListAuctionsResponse(auctions: Seq[ActorRef])
    case class BidOnNotification(auction: ActorRef)
  }
}
