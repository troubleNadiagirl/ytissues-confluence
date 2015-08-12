package ru.kontur.ytissues.settings

import com.atlassian.sal.api.pluginsettings.PluginSettings
import ru.kontur.ytissues.Constants

import scala.concurrent.duration.Duration
import scala.util.Try

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 05.08.2015
 */

/**
 * [[YtSettings]] is settings for connection with YouTrack
 * @param url base address of YouTrack
 * @param user YouTrack service user that perform requests
 * @param password users password
 * @param attempts attempts before YouTrack assumes unavailable
 * @param attemptTimeout timeout to GET-request attempt
 * @param unavailableDuration duration when YouTrack marked as unavailable
 */
case class YtSettings(url: String,
                      user: String,
                      password: String,
                      attempts: Int,
                      attemptTimeout: Duration,
                      unavailableDuration: Duration) {
  override def toString = "YtSettings(" +
    s"url = $url, user = $user, password = ****, attempts = $attempts, " +
    s"attemptTimeout = $attemptTimeout, unavailableDuration = $unavailableDuration)"

  def toYtProxySettings: YtProxySettings =
    YtProxySettings(attempts, unavailableDuration)

  def toYtClientSettings: YtClientSettings =
    YtClientSettings(url, user, password, attemptTimeout)
}

case class YtProxySettings(attempts: Int, unavailableDuration: Duration)

case class YtClientSettings(url: String, user: String, password: String, timeout: Duration) {
  override def toString: String =
    s"YtClientSettings(url = $url, user = $user, password = $password, timeout = $timeout)"
}

trait SettingsStorage {
  def ytSettings: Option[YtSettings]
  def ytSettings_=(settings: YtSettings)
}

// TODO(mp): cover with integration tests
class ConfluenceSettingsStorage(pluginSettings: PluginSettings) extends SettingsStorage {
  private val URL_KEY: String = "yturl"
  private val USER_KEY: String = "ytusername"
  private val PASSWORD_KEY: String = "ytpassword"
  private val ATTEMPT_TIMEOUT_KEY: String = "attempt-timeout"
  private val ATTEMPTS_KEY = "attempts"
  private val UNAVAILABLE_DURATION_KEY = "unavailable-duration"

  override def ytSettings: Option[YtSettings] =
    for {
      url <- get(URL_KEY)
      user <- get(USER_KEY)
      password <- get(PASSWORD_KEY)

      attemptsRaw <- get(ATTEMPTS_KEY)
      attempts <- Try { attemptsRaw.toInt }.toOption

      attemptsTimeoutRaw <- get(ATTEMPT_TIMEOUT_KEY)
      attemptsTimeout <- Try { Duration(attemptsTimeoutRaw) }.toOption

      unavailabilityDurationRaw <- get(UNAVAILABLE_DURATION_KEY)
      unavailabilityDuration <- Try { Duration(unavailabilityDurationRaw) }.toOption
    } yield YtSettings(url, user, password, attempts, attemptsTimeout, unavailabilityDuration)

  override def ytSettings_=(settings: YtSettings) = {
    put(URL_KEY, settings.url)
    put(USER_KEY, settings.user)
    put(PASSWORD_KEY, settings.password)

    put(ATTEMPTS_KEY, settings.attempts.toString)
    put(ATTEMPT_TIMEOUT_KEY, settings.attemptTimeout.toString)
    put(UNAVAILABLE_DURATION_KEY, settings.unavailableDuration.toString)
  }

  private def get(key: String) =
    Option(pluginSettings.get(s"${Constants.PLUGIN_SETTINGS_BASE_KEY}.$key")) match {
      case Some(x) => x match {
        case value: String => Some(value)
        case _ => None
      }
      case _ => None
    }

  private def put(key: String, value: String) =
    pluginSettings.put(s"${Constants.PLUGIN_SETTINGS_BASE_KEY}.$key", value)
}
