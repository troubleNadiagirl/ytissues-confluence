package ru.kontur.ytissues.client

import ru.kontur.ytissues.Issue

import scala.concurrent.Future

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 31.07.2015
 */
trait YtClient {
  def getIssue(id: String): Future[Option[Issue]]
}
