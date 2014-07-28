package actors.alchemy

import akka.actor.Actor
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.WS

import scala.concurrent.Future

class RankedKeywordsActor extends Actor {

  import models._
  import models.AlchemyJsonFormats._

  def processKeywords(url: String): Future[Seq[Keywords]] = {

    val futureKeywordsResponse = WS.url("http://access.alchemyapi.com/calls/url/URLGetRankedKeywords").withQueryString(
      "apikey" -> "95986d748333af9be9ecc31705253e7a61142d58",
      "url" -> url,
      "outputMode" -> "json").get() map {
      response =>
        response.json.validate[KeywordsResponse] match{
          case JsSuccess(keywordsResponse, _) => keywordsResponse
          case JsError(e) => throw new RuntimeException(e.toString())
        }
    }

    futureKeywordsResponse map {
      keywordsResponse =>
        keywordsResponse match {
          case KeywordsResponse(status, None, usage, url, language, Some(keywords)) => keywords.sortWith((a,b) => a.text < b.text )
          case KeywordsResponse(_, Some(statusInfo), _, _, _, _) => throw new RuntimeException(statusInfo)
        }
    }
  }

  def receive = {
    case RequestInfo(url) => {

      val futureKeywordsSeq = processKeywords(url)
      val futureSender = sender

      futureKeywordsSeq map {
        keywordsSeq =>
          play.api.Logger.info("Returning analysis result from ..." + url)
          futureSender ! keywordsSeq
      }
    }
  }
}
