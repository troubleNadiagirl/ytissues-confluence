package ru.kontur.ytissues.settings

import com.atlassian.sal.api.pluginsettings.PluginSettings
import ru.kontur.ytissues.Constants

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 05.08.2015
 */
case class YtSettings(url: String, user: String, password: String) {
  override def toString = s"YtSettings(host = $url, user = $user, password = ****)"
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

  override def ytSettings: Option[YtSettings] = {
    for {
      url <- get(URL_KEY)
      user <- get(USER_KEY)
      password <- get(PASSWORD_KEY)
    } yield YtSettings(url, user, password)
  }

  override def ytSettings_=(settings: YtSettings) = {
    put(URL_KEY, settings.url)
    put(USER_KEY, settings.user)
    put(PASSWORD_KEY, settings.password)
  }

  private def get(key: String) = {
    Option(pluginSettings.get(s"${Constants.PLUGIN_SETTINGS_BASE_KEY}.$key")) match {
      case Some(x) => x match {
        case value: String => Some(value)
        case _ => None
      }
      case _ => None
    }
  }

  private def put(key: String, value: String) = {
    pluginSettings.put(s"${Constants.PLUGIN_SETTINGS_BASE_KEY}.$key", value)
  }
}
