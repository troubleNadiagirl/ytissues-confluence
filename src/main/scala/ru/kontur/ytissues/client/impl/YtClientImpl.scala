package ru.kontur.ytissues.client.impl

import com.ning.http.client.{AsyncHttpClientConfig, AsyncHttpClient}
import dispatch._
import org.json4s.JsonAST.JValue
import ru.kontur.ytissues.client._
import ru.kontur.ytissues.exceptions.ConnectionException
import ru.kontur.ytissues.settings.YtClientSettings
import ru.kontur.ytissues.{Issue, Opened, Resolved}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

/**
 * @author Michael Plusnin <michael.plusnin@gmail.com>
 * @since 04.08.2015
 */
class YtClientImpl(private val settings: YtClientSettings)
                  (implicit ec: ExecutionContext) extends YtClient {
  private val base = url(settings.url)
  private val http = CreateDispatch()

  private def CreateDispatch(): Http = {
    val config = new AsyncHttpClientConfig.Builder()
      .setRequestTimeout(settings.timeout.toMillis.toInt)
      .build()

    Http(new AsyncHttpClient(config))
  }

  override def getIssue(id: String): Future[Option[Issue]] = {
    val request = (base / "rest" / "issue" / id)
      .addHeader("Accept", "application/json")
      .as_!(settings.user, settings.password)

    val x = http(request OK as.json4s.Json).either

    val p = Promise[Option[Issue]]()

    x onComplete {
      case Success(Right(json)) => p.complete(parse(json))
      case Success(Left(StatusCode(404))) => p.success(None)
      case Success(Left(e: java.util.concurrent.TimeoutException)) => p.failure(ConnectionException(e))
      case Success(Left(e: java.net.ConnectException)) => p.failure(ConnectionException(e))
      case Success(Left(t)) => p.failure(t)
      case Failure(t) => p.failure(t)
    }

    p.future
  }

  private def parse(json: JValue) : Try[Option[Issue]] = {
    import org.json4s._
    val idOpt = json \ "id" match {
      case JString(x) => Some(x)
      case _ => None
    }

    val summaryOpt: List[String] = for {
      JObject(field) <- json \ "field"
      if field contains ("name" -> JString("summary"))
      ("value", JString(s)) <- field
    } yield s

    val resolvedOpt: List[String] = for {
      JObject(field) <- json \ "field"
      if field contains ("name" -> JString("resolved"))
      ("value", JString(r)) <- field
    } yield r

    val status = resolvedOpt.headOption match {
      case Some(_) => Resolved
      case None => Opened
    }

    val parseOpt = for {
      id <- idOpt
      summary <- summaryOpt.headOption
    } yield Issue(id, summary, status)

    parseOpt match {
      case Some(x) => Success(Some(x))
      case None => Failure(new Exception("Can't parse issue"))
    }
  }
}
