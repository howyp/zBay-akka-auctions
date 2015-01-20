import Auction.Protocol._
import Auction.Running
import akka.actor.Props
import akka.pattern._
import org.joda.time.DateTime
import zBay.Protocol._

import scala.concurrent.Await.result

class zBaySpec extends ActorSpec {
  "The zBay" should {
    "be able to handle bids and return an auctions status" in {
      val auctionId = result(zBay ? AuctionCreateRequest(exampleEndTime), timeoutDuration).asInstanceOf[Long]
      result(zBay ? AuctionBidRequest(auctionId, userId = 1, value = 1.00), timeoutDuration)
      zBay ? AuctionStatusRequest(auctionId) must be_==(StatusResponse(1.00, Running)).await
    }
    "be able to query for auctions by date" in {
      val auctionId1 = result(zBay ? AuctionCreateRequest(twoDaysTime), timeoutDuration).asInstanceOf[Long]
      val auctionId2 = result(zBay ? AuctionCreateRequest(tomorrow), timeoutDuration).asInstanceOf[Long]
      val auctionId3 = result(zBay ? AuctionCreateRequest(tomorrow), timeoutDuration).asInstanceOf[Long]
      zBay ? AuctionQueryRequest(tomorrow) must be_==(AuctionQueryResponse(Set(auctionId2, auctionId3))).await
    }
  }

  val tomorrow = DateTime.now.plusDays(1)
  val twoDaysTime = DateTime.now.plusDays(2)

  override val config = """
                 |akka.actor.deployment {
                 |  /zBay/api {
                 |    router = round-robin
                 |    nr-of-instances = 5
                 |  }
                 |}""".stripMargin
  lazy val zBay = system.actorOf(Props[zBay], "zBay")
}
