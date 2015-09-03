package ru.kontur.ytissues.`macro`

import java.text.MessageFormat
import java.util
import java.util.regex.{Matcher, Pattern}

import com.atlassian.confluence.`macro`.Macro.{BodyType, OutputType}
import com.atlassian.confluence.`macro`.{Macro, MacroExecutionException}
import com.atlassian.confluence.content.render.xhtml.ConversionContext
import com.atlassian.sal.api.message.I18nResolver
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory
import com.atlassian.templaterenderer.TemplateRenderer
import com.atlassian.webresource.api.assembler.PageBuilderService
import ru.kontur.ytissues.Constants
import ru.kontur.ytissues.client.impl.YtClientCache
import ru.kontur.ytissues.settings.{ConfluenceSettingsStorage, SettingsStorage}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
 *
 * @author michael.plusnin
 */
class SingleMacroController(templater: TemplateRenderer,
                    i18n: I18nResolver,
                    pluginSettingsFactory: PluginSettingsFactory,
                    pageBuilderService: PageBuilderService) extends Macro {

  private val settingsStorage: SettingsStorage = {
    val pluginSettings = pluginSettingsFactory.createGlobalSettings
    new ConfluenceSettingsStorage(pluginSettings)
  }

  private val clientCache = new YtClientCache
  private val view = new SingleMacroView(i18n, templater, pageBuilderService)

  @throws(classOf[MacroExecutionException])
  override def execute(params: util.Map[String, String],
                       defaultParam: String,
                       cc: ConversionContext): String = {
    val rawIssueId = Option(params.get(Constants.ISSUE_ID_OR_URL_KEY))
    val issueId = parseIssueId(rawIssueId) match {
      case Right(id) => id
      case Left(e) => throw e
    }

    try {
      val settings = settingsStorage.ytSettings match {
        case Some(s) => s
        case None => throw new MacroExecutionException("Plugin isn't configured") // TODO: i18n
      }

      val client = clientCache.get(settings)
      val issue = Await.result(client.getIssue(issueId), Duration.Inf)

      view.renderXhtml(issueId, issue, settings)
    } catch {
      case e: MacroExecutionException => throw e
      case e: Exception => throw new MacroExecutionException(e)
    }
  }

  override def getBodyType: Macro.BodyType = BodyType.NONE

  override def getOutputType: Macro.OutputType = OutputType.INLINE

  /**
   * Parses issue ID from either issueId or from issueUrl
   * @param rawOpt raw issue ID
   * @return Right(issueId) or else Left(exception)
   */
  private def parseIssueId(rawOpt: Option[String]): Either[MacroExecutionException, String] = {
    rawOpt match {
      case Some(raw) =>
        val issueIdRE: String = "^[A-Za-z0-9]+\\-\\d+$"
        if (raw.matches(issueIdRE))
          Right(raw)
        else {
          // TODO: fix short project name regexp. Short project name can contain underline
          //                   (1   protocol    )     (2 hostname and port                 )
          //                                           (3 hostname            )    (4 port)
          val urlRegexp = "^(?:([A-Za-z\\.0-9-]+)://)?(([^/:]+|(?:\\[[^/]*\\]))(?::(\\w+))?)/" +
            // (                   some path             )       (5 issueId       )    (6 anchor)
            "(?:[A-Za-z0-9_\\.~!*'();@&=+$,?%\\[\\]-]*/)*issue/([A-Za-z0-9]+\\-\\d+)(?:#(.*))?$"

          val issueUrlPat: Pattern = Pattern.compile(urlRegexp)
          val m: Matcher = issueUrlPat.matcher(raw)
          if (!m.matches) Left(new MacroExecutionException(MessageFormat.format(
            i18n.getText(s"${Constants.PROJECT_BASE_KEY}.exceptionMessage.notYtIssueUrl", raw))))

          val issueId: String = m.group(5)
          if (issueId == null) Left(new MacroExecutionException(
            i18n.getText(s"${Constants.PROJECT_BASE_KEY}.exceptionMessage.issueNotFoundInUrl", raw)))

          Right(issueId)
        }
      case None => Left(new MacroExecutionException(
        i18n.getText(s"${Constants.PROJECT_BASE_KEY}.exceptionMessage.issueIdOrUrlNotDefined")))
    }
  }
}
