import Auction.Protocol._
import AuctionValue._
import User.Protocol.BidOnNotification
import BidStatus._
import akka.pattern._
import akka.testkit.{TestProbe, TestActorRef}

class AuctionSpec extends ActorSpec {
  "An auction" should {
    "be able to describe itself after it has been started" in {
      auction ? StatusRequest must be_==(NotSold).await
    }
    "accept a bid and update the current bid amount" in {
      auction ? Bid(1.00, user.ref) must be_==(Accepted).await
      auction ? StatusRequest must be_==(Sold(1.00)).await
    }
    "not accept a bid for lower than the current highest" in {
      auction ! Bid(1.00, user.ref)
      auction ! Bid(0.99, user.ref)
      auction ? StatusRequest must be_==(Sold(1.00)).await
    }
    "be able to tell if auction has finished" in {
      auction ! Bid(0.50, user.ref)
      auction ! EndNotification
      auction ? StatusRequest must be_==(Sold(0.50)).await
    }
    "ignore bids after auction finish" in {
      auction ! Bid(0.50, user.ref)
      auction ! EndNotification
      auction ? Bid(1.00, user.ref) must be_==(Rejected).await
      auction ? StatusRequest must be_==(Sold(0.50)).await
    }
    "tell the user that the bid was received" in {
      auction ! Bid(0.50, user.ref)
      user.receiveN(1).head must be_==(BidOnNotification(auction))
    }
    "be not won if no bids placed" in {
      auction ! EndNotification
      auction ? StatusRequest must be_==(NotSold).await
    }
  }

  val auction = TestActorRef(new Auction(exampleEndTime), "auction1")
  val user = TestProbe()
}
