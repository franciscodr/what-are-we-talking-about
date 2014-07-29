package actors.alchemy

import akka.actor.Actor
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.WS

import scala.concurrent.Future

class PageTextActor extends Actor {

  import models._
  import models.AlchemyJsonFormats._

  def processText(url: String): Future[String] = {

    val futureTextResponse = WS.url("http://access.alchemyapi.com/calls/url/URLGetText").withQueryString(
      "apikey" -> "95986d748333af9be9ecc31705253e7a61142d58",
      "url" -> url,
      "outputMode" -> "json").get() map {
      response =>
        response.json.validate[TextResponse] match{
          case JsSuccess(textResponse, _) => textResponse
          case JsError(e) => throw new RuntimeException(e.toString())
        }
    }

    futureTextResponse map {
      textResponse =>
        textResponse match {
          case TextResponse(status, None, usage, url, text) => text
          case TextResponse(_, Some(statusInfo), _, _, _) => throw new RuntimeException(statusInfo)
        }
    }
  }

  def receive = {
    case RequestInfo(url) => {

      val futureText = processText(url)
      val futureSender = sender

      futureText map {
        text =>
          play.api.Logger.info("Returning analysis result from ..." + url)
          futureSender ! text
      }
    }
  }
}
