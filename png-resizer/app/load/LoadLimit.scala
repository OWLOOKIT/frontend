package load

import java.util.concurrent.atomic.AtomicInteger

import common.{ExecutionContexts, Logging}
import model.Cached
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

object LoadLimit extends ExecutionContexts with Logging {

  private lazy val currentNumberOfRequests = new AtomicInteger(0)

  // temporary count just to check things are working as expected
  private lazy val currentNumberOfResizes = new AtomicInteger(0)

  private lazy val requestLimit = {
    val limit = Runtime.getRuntime.availableProcessors() * 2
    // one per cpu is too low as we have to wait for the content to download, so go for 2 per cpu
    log.info(s"request limit set to $limit")
    limit
  }

  def apply(fallbackUri: String)(available: =>  Future[Result]): Future[Result] = try {
    val concurrentRequests = currentNumberOfRequests.incrementAndGet
    if (concurrentRequests <= requestLimit) try {
      log.info(s"Resize ${currentNumberOfResizes.incrementAndGet()}/$requestLimit")
      val result = available
      result.onComplete{_ =>
        currentNumberOfRequests.decrementAndGet()
        currentNumberOfResizes.decrementAndGet()
      }
      result
    } catch {
      case t: Throwable =>
        currentNumberOfRequests.decrementAndGet()
        throw t
    } else {
      currentNumberOfRequests.decrementAndGet()
      Future.successful(Cached(60)(TemporaryRedirect(fallbackUri)))
    }
  }
}
