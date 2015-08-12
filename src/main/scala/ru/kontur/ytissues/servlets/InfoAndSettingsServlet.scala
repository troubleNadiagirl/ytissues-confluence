package ru.kontur.ytissues.servlets

import java.io.IOException
import java.net.URI
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.atlassian.sal.api.auth.LoginUriProvider
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory
import com.atlassian.sal.api.user.UserManager
import com.atlassian.templaterenderer.TemplateRenderer
import ru.kontur.ytissues.settings.{ConfluenceSettingsStorage, SettingsStorage, YtSettings}

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration
import scala.util.Try

/**
 *
 * @author michael.plusnin
 */
class InfoAndSettingsServlet(userManager: UserManager,
                             templateRenderer: TemplateRenderer,
                             loginUriProvider: LoginUriProvider,
                             pluginSettingsFactory: PluginSettingsFactory) extends HttpServlet {
  private val settingsStorage: SettingsStorage =
    new ConfluenceSettingsStorage(pluginSettingsFactory.createGlobalSettings)

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    if (!isSysAdminRequest(request)) {
      redirectToLogin(request, response)
      return
    }

    val settings = settingsStorage.ytSettings.getOrElse(YtSettings(
      url = "", user = "", password = "", attempts = 0,
      attemptTimeout = Duration.Undefined, unavailableDuration = Duration.Undefined))

    val substitution = { s: YtSettings => Map(
      "url" -> s.url,
      "user" -> s.user,
      "password" -> "",
      "attempts" -> Int.box(s.attempts),
      "timeout" -> s.attemptTimeout,
      "unavailableDuration" -> s.unavailableDuration) } apply settings

    response.setContentType("text/html:charset=utf-8")
    templateRenderer.render("admin.vm", substitution, response.getWriter)
  }

  override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    if (!isSysAdminRequest(request)) {
      redirectToLogin(request, response)
      return
    }

    val requestParameter = { name: String => Option(request.getParameter(name)) }

    val oldSettings = settingsStorage.ytSettings

    // Take old password if URL not changed
    val selectPassword = {
      (nextUrl: String, nextPassword: String) =>
        oldSettings match {
          case None => nextPassword
          case Some(stored) =>
            if (nextPassword == "") {
              if (stored.url == nextUrl)
                stored.password
              else
                ""
            } else
              nextPassword
        }
    }

    val nextSettings = for {
      url <- requestParameter("url")
      user <- requestParameter("user")
      password <- requestParameter("password").orElse(Some(""))
      attempts <- requestParameter("attempts").flatMap(a => Try { a.toInt }.toOption)
      attemptTimeout <- requestParameter("timeout").flatMap(t => Try { Duration(t) }.toOption)

      unavailableDurationRaw <- requestParameter("unavailableDuration")
      unavailableDuration <- Try { Duration(unavailableDurationRaw)}.toOption
    } yield YtSettings(url, user, selectPassword(url, password), attempts = attempts,
        attemptTimeout = attemptTimeout, unavailableDuration = unavailableDuration)

    nextSettings.foreach(s => settingsStorage.ytSettings = s)
    response.sendRedirect("ytissues")
  }

  private def isSysAdminRequest(request: HttpServletRequest): Boolean = {
    val username: String = userManager.getRemoteUsername(request)
    username != null && userManager.isSystemAdmin(username)
  }

  @throws(classOf[IOException])
  private def redirectToLogin(request: HttpServletRequest, response: HttpServletResponse) {
    response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString)
  }

  private def getUri(request: HttpServletRequest): URI = {
    val builder: StringBuffer = request.getRequestURL
    if (request.getQueryString != null) {
      builder.append("?")
      builder.append(request.getQueryString)
    }
    URI.create(builder.toString)
  }
}
