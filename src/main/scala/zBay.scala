import org.joda.time.DateTime

import scala.concurrent.Future
import scala.math.BigDecimal

trait zBay {
  def createAuction(endTime: DateTime): Long
  def status(auctionId: Long): Future[AuctionValue]
  def placeBid(auctionId: Long, userId: Long, value: BigDecimal): Future[BidStatus]
  def find(endTime: DateTime): Future[Set[Long]]
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
