import akka.actor.ActorSystem
import akka.pattern._
import akka.testkit.TestActorRef
import org.specs2.mutable.Specification
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import org.specs2.time.NoTimeConversions
import Auction.Protocol._
import User.Protocol._
import org.joda.time.DateTime

class AuctionSpec extends Specification
                     with NoTimeConversions { isolated
  "An auction" should {
    "be able to describe itself after it has been started" in {
      auction ? StatusRequest must be_==(StatusResponse(0.00, Running)).await
    }
    "accept a bid and update the current bid amount" in {
      auction ! Bid(1.00, user)
      auction ? StatusRequest must be_==(StatusResponse(1.00, Running)).await
    }
    "not accept a bid for lower than the current highest" in {
      auction ! Bid(1.00, user)
      auction ! Bid(0.99, user)
      auction ? StatusRequest must be_==(StatusResponse(1.00, Running)).await
    }
    "be able to tell if auction has finished" in {
      auction ! EndNotification
      auction ? StatusRequest must be_==(StatusResponse(0, Ended)).await
    }
    "ignore bids after auction finish" in {
      auction ! Bid(0.50, user)
      auction ! EndNotification
      auction ! Bid(1.00, user)
      auction ? StatusRequest must be_==(StatusResponse(0.50, Ended)).await
    }
    "tell the user actor that the bid was received" in {
      auction ! Bid(0.50, user)
      user ? ListAuctionsRequest must be_==(ListAuctionsResponse(Seq(auction))).await
    }
  }

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(3, TimeUnit.SECONDS)

  val exampleEndTime = DateTime.now.plusDays(7)

  lazy val auction = TestActorRef(new Auction(exampleEndTime))
  lazy val user = TestActorRef(new User)
}
