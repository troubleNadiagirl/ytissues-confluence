package ru.kontur.ytissues.settings

import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 04.08.2015
 */
class YtSettingsSpec extends WordSpec with Matchers {
  "A settings" when {
    val settings = YtSettings("example", "theUserName", "thePassword",
      attemptTimeout = 2.milliseconds, attempts = 5, unavailableDuration = 30.seconds)

    "call toString" should {
      "not contains password" in {
        settings.toString should not include "thePassword"
      }

      "have example format" in {
        settings.toString shouldBe "YtSettings(" +
          "url = example, user = theUserName, password = ****, " +
          "attempts = 5, attemptTimeout = 2 milliseconds, unavailableDuration = 30 seconds)"
      }
    }

    "call toYtClientSettings" should {
      "return YtClientSettings" in {
        settings.toYtClientSettings shouldBe
          YtClientSettings("example", "theUserName", "thePassword", 2.milliseconds)
      }
    }

    "call toYtProxySettings" should {
      "return YtProxySettings" in {
        settings.toYtProxySettings shouldBe YtProxySettings(
          attempts = 5, unavailableDuration = 30.seconds)
      }
    }
  }

  "A YtClientSettings" when {
    val ytClientSettings = YtClientSettings("http://url", "theUser", "thePassword", 1.second)

    "call toString" should {
      "have example format" in {
        ytClientSettings.toString shouldBe "YtClientSettings(" +
          "url = http://url, user = theUser, password = thePassword, timeout = 1 second)"
      }
    }
  }
}
