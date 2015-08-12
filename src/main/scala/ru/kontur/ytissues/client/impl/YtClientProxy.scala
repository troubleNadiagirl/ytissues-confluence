package ru.kontur.ytissues.client.impl

import org.joda.time.{DateTime, Duration}
import ru.kontur.ytissues.client.YtClient
import ru.kontur.ytissues.exceptions.{TimeoutException, ServiceUnavailableException}
import ru.kontur.ytissues.settings.YtProxySettings
import ru.kontur.ytissues.Issue

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 11.08.2015
 */
class YtClientProxy(private val settings: YtProxySettings,
                    private val proxied: YtClient)(implicit ec: ExecutionContext) extends YtClient {
  @volatile var makedAttempts = 0
  @volatile var lastOvercountAttempt: Option[DateTime] = None

  override def getIssue(id: String): Future[Option[Issue]] = {
    if (isUnavailable)
      Future.failed(ServiceUnavailableException())
    else
      request(id)
  }

  def isUnavailable: Boolean = synchronized { !isAvailableUnsafe }

  private def request(id: String): Future[Option[Issue]] = {
    val p = Promise[Option[Issue]]()
    proxied.getIssue(id) onComplete {
      case Success(x) => p.success(x)
      case Failure(x : TimeoutException) => p.failure(x); failAttempt()
      case Failure(x) => p.failure(x)
    }

    p.future
  }

  private def failAttempt(): Unit = {
    synchronized {
      if (isAvailableUnsafe)
        makedAttempts += 1

      if (makedAttempts >= settings.attempts) {
        makedAttempts = 0
        lastOvercountAttempt = Some(DateTime.now())
      }
    }
  }

  private def isAvailableUnsafe: Boolean = {
    lastOvercountAttempt match {
      case None => true
      case Some(last) =>
        new Duration(last, DateTime.now()).getMillis.millis > settings.unavailableDuration
    }
  }
}
