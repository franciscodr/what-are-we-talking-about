package actors.alchemy

import akka.actor.Actor
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.WS

import scala.concurrent.Future

class PageTitleActor extends Actor {

  import models._
  import models.AlchemyJsonFormats._

  def processTitle(url: String): Future[String] = {

    val futureTitleResponse = WS.url("http://access.alchemyapi.com/calls/url/URLGetTitle").withQueryString(
      "apikey" -> "95986d748333af9be9ecc31705253e7a61142d58",
      "url" -> url,
      "outputMode" -> "json").get() map {
      response =>
        response.json.validate[TitleResponse] match{
          case JsSuccess(titleResponse, _) => titleResponse
          case JsError(e) => throw new RuntimeException(e.toString())
        }
    }

    futureTitleResponse map {
      titleResponse =>
        titleResponse match {
          case TitleResponse(status, None, usage, url, title) => title
          case TitleResponse(_, Some(statusInfo), _, _, _) => throw new RuntimeException(statusInfo)
        }
    }
  }

  def receive = {
    case RequestInfo(url) => {

      val futureTitle = processTitle(url)
      val futureSender = sender

      futureTitle map {
        title =>
          play.api.Logger.info("Returning analysis result from ..." + url)
          futureSender ! title
      }
    }
  }
}
