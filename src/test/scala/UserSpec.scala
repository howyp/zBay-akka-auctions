import akka.pattern._
import akka.testkit._
import User.Protocol._

class UserSpec extends ActorSpec {
  "A user" should {
    "start empty" in {
      user ? ListAuctionsRequest must be_==(ListAuctionsResponse(Nil)).await
    }
    "list auctions bid on" in {
      user ! BidOnNotification(auction(1))
      user ! BidOnNotification(auction(2))
      user ? ListAuctionsRequest must be_==(ListAuctionsResponse(List(auction(2),auction(1)))).await
    }
  }
  val user = TestActorRef(new User)
  val auction = (1 to 3).reverse.map(i => TestActorRef(s"auction$i"))
}
