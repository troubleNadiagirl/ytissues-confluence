package ru.kontur.ytissues.client

import ru.kontur.ytissues.Issue
import ru.kontur.ytissues.client.impl.YtClientImpl
import ru.kontur.ytissues.settings.YtSettings

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 31.07.2015
 */
object YtClient {
  def apply(settings: YtSettings)(implicit ec: ExecutionContext) = new YtClientImpl(settings)
}

trait YtClient {
  def getIssue(id: String): Future[Option[Issue]]
}
