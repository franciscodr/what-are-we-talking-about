package actors.alchemy

import akka.actor.Actor
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.WS

import scala.concurrent.Future

class RankedNamedEntitiesActor extends Actor {

  import models._
  import models.AlchemyJsonFormats._

  def processEntities(url: String) = {

    val futureEntitiesResponse = WS.url("http://access.alchemyapi.com/calls/url/URLGetRankedNamedEntities").withQueryString(
      "apikey" -> "95986d748333af9be9ecc31705253e7a61142d58",
      "url" -> url,
      "outputMode" -> "json").get() map {
      response =>
        response.json.validate[EntitiesResponse] match{
          case JsSuccess(entitiesResponse, _) => entitiesResponse
          case JsError(e) => throw new RuntimeException(e.toString())
        }
    }

    futureEntitiesResponse map {
      entitiesResponse =>
        entitiesResponse match {
          case EntitiesResponse(status, None, usage, url, language, Some(entities)) => entities.sortWith((a,b) => a.count > b.count )
          case EntitiesResponse(_, Some(statusInfo), _, _, _, _) => throw new RuntimeException(statusInfo)
        }
      }
  }

  def receive = {
    case RequestInfo(url) => {

      /*val futureEntitiesSeq = for {
        entitiesSeq <- processEntities(url)
      } yield entitiesSeq

      val result = processEntities(url)
      result*/
      val futureEntitiesSeq = processEntities(url)
      val futureSender = sender

      futureEntitiesSeq map {
        entitiesSeq =>
          play.api.Logger.info("Returning analysis result from ..." + url)
          futureSender ! entitiesSeq
      }
    }
  }
}
