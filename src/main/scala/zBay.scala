import org.joda.time.DateTime

import scala.concurrent.Future
import scala.math.BigDecimal

trait zBay {
  def createAuction(endTime: DateTime): AuctionId
  def status(auctionId: AuctionId): Future[AuctionValue]
  def placeBid(auctionId: AuctionId, userId: UserId, value: BigDecimal): Future[BidStatus]
  def find(endTime: DateTime): Future[Set[AuctionId]]
}

sealed trait AuctionValue
object AuctionValue {
  case class Sold(highestBid: BigDecimal) extends AuctionValue
  case object NotSold extends AuctionValue
}

trait BidStatus
object BidStatus {
  case object Accepted extends BidStatus
  case object Rejected extends BidStatus
}

case class AuctionId(id: Long) extends AnyVal {
  override def toString = id.toString
}
case class UserId(id: Long) extends AnyVal {
  override def toString = id.toString
}