import AuctionValue.Sold
import akka.actor.{TypedActor, TypedProps}
import org.joda.time.DateTime

import scala.concurrent.Await.result

class zBaySpec extends ActorSpec {
  "The zBay" should {
    "be able to handle bids and return an auctions status" in {
      val auctionId = zBay.createAuction(exampleEndTime)
      result(zBay.placeBid(auctionId, userId = 1, value = 1.00), timeoutDuration)
      zBay.status(auctionId) must be_==(Sold(1.00)).await
    }
    "be able to query for auctions by date" in {
      val auctionId1 = zBay.createAuction(twoDaysTime)
      val auctionId2 = zBay.createAuction(tomorrow)
      val auctionId3 = zBay.createAuction(tomorrow)
      zBay.find(tomorrow) must be_==(Set(auctionId2, auctionId3)).await
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
  lazy val zBay: zBay = TypedActor(system).typedActorOf(TypedProps[zBayTypedActor](), "zBay")
}
