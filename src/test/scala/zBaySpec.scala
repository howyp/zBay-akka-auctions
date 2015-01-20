import Auction.Protocol._
import akka.actor.Props
import akka.pattern._
import zBay.Protocol._

import scala.concurrent.Await.result

class zBaySpec extends ActorSpec {
  "The zBay" should {
    "be able to handle bids and return an auctions status" in {
      val auctionId = result(zBay ? AuctionCreateRequest(exampleEndTime), timeoutDuration).asInstanceOf[Long]
      zBay ! AuctionBidRequest(auctionId = auctionId,
                               userId    = 1,
                               value     = 1.00)
      zBay ? AuctionStatusRequest(auctionId) must be_==(StatusResponse(1.00, Running)).await
    }
  }

  val zBay = system.actorOf(Props[zBay])
}
