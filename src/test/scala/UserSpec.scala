import akka.pattern._
import akka.testkit._
import User.Protocol._

class UserSpec extends ActorSpec {
  "A user" should {
    "start empty" in {
      user ? ListAuctionsRequest must be_==(ListAuctionsResponse(Nil)).await
    }
    "list auctions bid on" in {
      user ! BidOnNotification(auction)
      user ? ListAuctionsRequest must be_==(ListAuctionsResponse(List(auction))).await
    }
  }
  val user = TestActorRef(new User)
  val auction = TestActorRef("auction")
}
