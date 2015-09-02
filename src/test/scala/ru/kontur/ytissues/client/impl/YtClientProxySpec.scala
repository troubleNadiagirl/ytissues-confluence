package ru.kontur.ytissues.client.impl

import org.scalatest.concurrent.Futures
import org.scalatest.{Matchers, WordSpec}
import ru.kontur.ytissues.client.YtClient
import ru.kontur.ytissues.exceptions.ConnectionException
import ru.kontur.ytissues.settings.YtProxySettings
import ru.kontur.ytissues.{Opened, Issue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 11.08.2015
 */
class YtClientProxySpec extends WordSpec with Matchers with Futures {
  "A YtClientProxy" when {
    val settings = YtProxySettings(
      attempts = 2, unavailableDuration = 5.seconds)
    class UnavailableProxy {
      val proxy = new YtClientProxy(settings, new YtClient {
        override def getIssue(id: String) = {
          Thread.sleep(500)
          Future.failed(ConnectionException(new Exception()))}})

      def makeRequest(): Future[Option[Issue]] = proxy.getIssue("USELESS-1")
    }

    "unavailable" should {
      "makes [[attempts]] before marked as unavailable" in new UnavailableProxy {
        proxy.isUnavailable shouldBe false
        Await.ready(makeRequest(), 1.second)
        proxy.isUnavailable shouldBe false
        Await.ready(makeRequest(), 1.second)
        proxy.isUnavailable shouldBe true
      }

      "waits [[unavailableTimeout]] before marked as available" in new UnavailableProxy {
        Await.ready(makeRequest(), 1.second)
        Await.ready(makeRequest(), 1.second)
        proxy.isUnavailable shouldBe true
        Thread.sleep(7000)
        proxy.isUnavailable shouldBe false
      }
    }

    class AvailableProxy {
      val proxy = new YtClientProxy(settings, new YtClient {
        override def getIssue(id: String) = Future { Some(Issue(id, "summary", Opened)) }
      })
    }

    "available" should {
      "return content" in new AvailableProxy {
        Await.result(proxy.getIssue("ISSUE-1"), 1.second) shouldBe
          Some(Issue("ISSUE-1", "summary", Opened))
      }
    }
  }
}
