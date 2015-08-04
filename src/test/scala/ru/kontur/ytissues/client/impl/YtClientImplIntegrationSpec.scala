package ru.kontur.ytissues.client.impl

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.SpanSugar._
import ru.kontur.ytissues.client._

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 31.07.2015
 */
class YtClientImplIntegrationSpec extends WordSpec with Matchers with ScalaFutures {
  implicit val defaultPatience = PatienceConfig(timeout = 1 seconds, interval = 5 millis)

  "A YouTrack client" when {
    val youTrack = new YtClientImpl(new YtSettings("root", "123", "192.168.99.100:32768"))

    "non contains issue" should {
      "returns None within 1 second" in {
        whenReady(youTrack.getIssue("NOTFOUND-1")) { _ shouldBe None }
      }
    }

    "contains issue" should {
      "returns opened issue within 1 second" in {
        whenReady(youTrack.getIssue("FOUND-1")) {
          _ shouldBe Some(Issue("FOUND-1", "A summary for FOUND-1", Opened))
        }
      }

      "returns resolved issue within 1 second" in {
        whenReady(youTrack.getIssue("FOUND-2")) {
          _ shouldBe Some(Issue("FOUND-2", "A summary for FOUND-2", Resolved))
        }
      }
    }

    "server unavailable" should {
      val unavailableYouTrack = new YtClientImpl(new YtSettings("1", "1", "unavailable:123"))
      "return failure" in {
        whenReady(unavailableYouTrack.getIssue("USELESS-1").failed) { _ shouldBe a [Exception] }
      }
    }
  }
}
