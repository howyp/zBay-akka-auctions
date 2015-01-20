import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions

import scala.concurrent.duration.FiniteDuration

trait ActorSpec extends Specification with NoTimeConversions {
  isolated

  implicit val system = ActorSystem()
  val timeoutDuration = FiniteDuration(3, TimeUnit.SECONDS)
  implicit val timeout = Timeout(timeoutDuration)

  val exampleEndTime = DateTime.now.plusDays(7)
}
