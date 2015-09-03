package ru.kontur.ytissues.`macro`

import java.io.StringWriter

import com.atlassian.sal.api.message.I18nResolver
import com.atlassian.templaterenderer.TemplateRenderer
import com.atlassian.webresource.api.assembler.PageBuilderService
import ru.kontur.ytissues._
import ru.kontur.ytissues.settings.YtSettings

import scala.collection.JavaConversions._

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 03.09.2015
 */
class SingleMacroView(private val i18n: I18nResolver,
                      private val templater: TemplateRenderer,
                      private val pageBuilderService: PageBuilderService) {
  def renderXhtml(issueId: String, issueOpt: Option[Issue], settings: YtSettings): String = {
    requireResource(s"${Constants.PROJECT_BASE_KEY}:cssResource")

    val urlComposer: IssueUrlComposer = new IssueUrlComposer(settings.url)
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
    templater.render("yt-single-issue.vm", baseSubstitution ++ substitution, sw)
    sw.toString
  }

  /**
   * Requires web resource as in {{com.atlassian.confluence.setup.velocity.VelocityFriendlyPageBuilderService}}
   * @param resourceKey is complete resource key
   */
  private def requireResource(resourceKey: String): Unit = {
    pageBuilderService.assembler().resources().requireWebResource(resourceKey)
  }
}
