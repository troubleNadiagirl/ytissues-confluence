package ru.kontur.ytissues.client.impl

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.SpanSugar._
import ru.kontur.ytissues.exceptions.TimeoutException
import ru.kontur.ytissues.settings.YtClientSettings
import ru.kontur.ytissues.{Issue, Opened, Resolved}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 31.07.2015
 */
class YtClientImplIntegrationSpec extends WordSpec with Matchers with ScalaFutures {
  implicit val defaultPatience = PatienceConfig(timeout = 1.seconds, interval = 5.millis)

  "A YouTrack client" when {
    val connectionSettings = YtClientSettings("http://192.168.99.100:32768", "root", "123", 1.second)
    val youTrack = new YtClientImpl(connectionSettings)

    "non contains issue" should {
      "returns None" in {
        whenReady(youTrack.getIssue("NOTFOUND-1")) { _ shouldBe None }
      }
    }

    "contains issue" should {
      "returns opened issue" in {
        whenReady(youTrack.getIssue("FOUND-1")) {
          _ shouldBe Some(Issue("FOUND-1", "First", Opened))
        }
      }

      "returns resolved issue within" in {
        whenReady(youTrack.getIssue("FOUND-2")) {
          _ shouldBe Some(Issue("FOUND-2", "Second", Resolved))
        }
      }
    }

    "response won't came in timeout" should {
      val slowSettings = YtClientSettings("http://yt-test", "root", "123", 1.second)
      val slowYouTrack = new YtClientImpl(slowSettings)
      "return TimeoutException" in {
        Await.result(slowYouTrack.getIssue("USELESS-1").failed, 10.seconds) shouldBe
          a [TimeoutException]
      }
    }

    "server not found" should {
      val unknownSettings = YtClientSettings("http://unavailable:123", "1", "1", 1.second)
      val unavailableYouTrack = new YtClientImpl(unknownSettings)
      "return failure" in {
        whenReady(unavailableYouTrack.getIssue("USELESS-1").failed) { _ shouldBe an [Exception] }
      }
    }
  }
}
