package ru.kontur.ytissues

import java.io.StringWriter
import java.text.MessageFormat
import java.util
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.{Matcher, Pattern}

import com.atlassian.confluence.`macro`.Macro.{BodyType, OutputType}
import com.atlassian.confluence.`macro`.{Macro, MacroExecutionException}
import com.atlassian.confluence.content.render.xhtml.ConversionContext
import com.atlassian.plugin.webresource.WebResourceManager
import com.atlassian.sal.api.message.I18nResolver
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory
import com.atlassian.templaterenderer.TemplateRenderer
import ru.kontur.ytissues.client.YtClient
import ru.kontur.ytissues.client.impl.{YtClientImpl, YtClientProxy}
import ru.kontur.ytissues.settings.{ConfluenceSettingsStorage, SettingsStorage, YtSettings}

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
 *
 * @author michael.plusnin
 */
class SingleYtMacro(templateRenderer: TemplateRenderer,
                    i18n: I18nResolver,
                    pluginSettingsFactory: PluginSettingsFactory,
                    webResourceManager: WebResourceManager) extends Macro {
  private val settingsStorage: SettingsStorage = {
    val pluginSettings = pluginSettingsFactory.createGlobalSettings
    new ConfluenceSettingsStorage(pluginSettings)
  }

  private def createClient(settings: Option[YtSettings]): Option[YtClient] = {
    for {
      s <- settings
      backClient = new YtClientImpl(s.toYtClientSettings)
    } yield new YtClientProxy(s.toYtProxySettings, backClient)
  }

  /** Don't use this! Is settings changed this may be not changed */
  private val old: AtomicReference[Option[(YtSettings, YtClient)]] =
    new AtomicReference[Option[(YtSettings, YtClient)]](None)

  // TODO: extract to class
  private def current: Option[(YtSettings, YtClient)] = {
    val settingsFromStorage = settingsStorage.ytSettings
    if (settingsFromStorage != old.get().map(_._1)) {
      old.set(for {
        s <- settingsFromStorage
        c <- createClient(settingsFromStorage)
      } yield (s, c))
    }

    old.get()
  }

  @throws(classOf[MacroExecutionException])
  override def execute(params: util.Map[String, String], defaultParam: String, cc: ConversionContext): String = {
    webResourceManager.requireResource(s"${Constants.PROJECT_BASE_KEY}:cssResource")
    val issueIdOrUrl: String = params.get(Constants.ISSUE_ID_OR_URL_KEY)
    if (issueIdOrUrl == null) throw new MacroExecutionException(
      i18n.getText(s"${Constants.PROJECT_BASE_KEY}.exceptionMessage.issueIdOrUrlNotDefined"))

    val issueIdRE: String = "^[A-Za-z0-9]+\\-\\d+$"
    val issueId = if (issueIdOrUrl.matches(issueIdRE)) {
      issueIdOrUrl
    } else {
      //                   (1   protocol    )     (2 hostname and port                 )
      //                                           (3 hostname            )    (4 port)
      val urlRegexp = "^(?:([A-Za-z\\.0-9-]+)://)?(([^/:]+|(?:\\[[^/]*\\]))(?::(\\w+))?)/" +
      // (                   some path             )       (5 issueId       )    (6 anchor)
        "(?:[A-Za-z0-9_\\.~!*'();@&=+$,?%\\[\\]-]*/)*issue/([A-Za-z0-9]+\\-\\d+)(?:#(.*))?$"

      val issueUrlPat: Pattern = Pattern.compile(urlRegexp)
      val m: Matcher = issueUrlPat.matcher(issueIdOrUrl)
      if (!m.matches) throw new MacroExecutionException(MessageFormat.format(
        i18n.getText(s"${Constants.PROJECT_BASE_KEY}.exceptionMessage.notYtIssueUrl", issueIdOrUrl)))

      val issueId: String = m.group(5)
      if (issueId == null) throw new MacroExecutionException(
        i18n.getText(s"${Constants.PROJECT_BASE_KEY}.exceptionMessage.issueNotFoundInUrl", issueIdOrUrl))

      issueId
    }

    try
      getIssueXhtmlElement(issueId)
    catch {
      case e: Exception => throw new MacroExecutionException(e)
    }
  }

  @throws(classOf[Exception])
  private def getIssueXhtmlElement(issueId: String): String = {
    val (settings, client) = current match {
      case Some((s, c)) => (s, c)
      case None => throw new Exception("Settings not found")
    }

    val urlComposer: IssueUrlComposer = new IssueUrlComposer(settings.url)
    val issueOpt: Option[Issue] = Await.result(client.getIssue(issueId), Duration.Inf)

    val baseSubstitution = Map("id" -> issueId.toUpperCase, "ref" -> urlComposer.compose(issueId))

    val substitution = issueOpt match {
      case Some(issue) => Map(
        "summary" -> issue.summary,
        "status" -> (issue.state match {
          case Opened => i18n.getText(s"${Constants.PROJECT_BASE_KEY}.openedStatus")
          case Resolved => i18n.getText(s"${Constants.PROJECT_BASE_KEY}.closedStatus")
        }),
        "statusCssType" -> (issue.state match {
          case Opened => "ytopened"
          case Resolved => "ytclosed"
        }))
      case None => Map(
        "summary" -> "",
        "status" -> i18n.getText(s"${Constants.PROJECT_BASE_KEY}.notExistsStatus"),
        "statusCssType" -> "ytnotexists")
    }

    val sw: StringWriter = new StringWriter
    templateRenderer.render("yt-single-issue.vm", baseSubstitution ++ substitution, sw)
    sw.toString
  }

  override def getBodyType: Macro.BodyType = BodyType.NONE

  override def getOutputType: Macro.OutputType = OutputType.INLINE
}
