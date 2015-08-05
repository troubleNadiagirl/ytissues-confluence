package ru.kontur.ytissues.settings

import org.scalatest.{Matchers, WordSpec}

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 04.08.2015
 */
class YtSettingsSpec extends WordSpec with Matchers {
  "A settings" when {
    val settings = YtSettings("example", "theUserName", "thePassword")

    "call toString" should {
      "not contains password" in {
        settings.toString should not include "thePassword"
      }
    }
  }
}
