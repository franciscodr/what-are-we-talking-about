package actors

import akka.actor.Actor
import models.{WebPageTextAnalysis, TextResponse, KeywordsResponse, EntitiesResponse}
import models.AlchemyJsonFormats._
import org.slf4j.{LoggerFactory, Logger}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.WS

import scala.concurrent.Future

case class Num(value : Int)
case class UrlInfo(url : String)

class AlchemyActor extends Actor {

  def processEntities(url: String): Future[EntitiesResponse] = {
    WS.url("http://access.alchemyapi.com/calls/url/URLGetRankedNamedEntities").withQueryString(
      "apikey" -> "95986d748333af9be9ecc31705253e7a61142d58",
      "url" -> url,
      "outputMode" -> "json").get() map {
      response =>
        response.json.validate[EntitiesResponse] match{
          case JsSuccess(entitiesResponse, _) => entitiesResponse
          case JsError(e) => throw new RuntimeException(e.toString())
        }
    }
  }

  def processInfo(url: String, textResponse : TextResponse, entitiesResponse : EntitiesResponse, keywordsResponse : KeywordsResponse): Future[WebPageTextAnalysis] = {
    val title = textResponse match {
      case TextResponse(status, None, usage, url, text) => text
      case TextResponse(_, Some(statusInfo), _, _, _) => throw new RuntimeException(statusInfo)
      case _ => throw new RuntimeException()
    }

    val entities = entitiesResponse match {
      case EntitiesResponse(status, None, usage, url, language, Some(entities)) => entities.sortWith((a,b) => a.count > b.count )
      case EntitiesResponse(_, Some(statusInfo), _, _, _, _) => throw new RuntimeException(statusInfo)
    }

    val keywords = keywordsResponse match {
      case KeywordsResponse(status, None, usage, url, language, Some(keywords)) => keywords.sortWith((a,b) => a.text < b.text )
      case KeywordsResponse(_, Some(statusInfo), _, _, _, _) => throw new RuntimeException(statusInfo)
    }

    Future.successful(new WebPageTextAnalysis(url, title, entities, keywords))
  }

  def processKeywords(url: String): Future[KeywordsResponse] = {
    WS.url("http://access.alchemyapi.com/calls/url/URLGetRankedKeywords").withQueryString(
      "apikey" -> "95986d748333af9be9ecc31705253e7a61142d58",
      "url" -> url,
      "outputMode" -> "json").get() map {
      response =>
        response.json.validate[KeywordsResponse] match{
          case JsSuccess(keywordsResponse, _) => keywordsResponse
          case JsError(e) => throw new RuntimeException(e.toString())
        }
    }
  }

  def processText(url: String): Future[TextResponse] = {
    play.api.Logger.info("Retrieving text info from ..." + url)
    WS.url("http://access.alchemyapi.com/calls/url/URLGetText").withQueryString(
      "apikey" -> "95986d748333af9be9ecc31705253e7a61142d58",
      "url" -> url,
      "outputMode" -> "json").get() map {
      response =>
        response.json.validate[TextResponse] match{
          case JsSuccess(textResponse, _) => {
            play.api.Logger.info("Returning text info from ..." + url)
            textResponse
          }
          case JsError(e) => throw new RuntimeException(e.toString())
        }
    }
  }

  def receive = {
    case Num(v) => sender ! (v * 2)
    case UrlInfo(url) => {
      play.api.Logger.info("Retrieving data from ..." + url)
      val futureAnalysisResult = for {
        textResponse <- processText(url)
        entitiesResponse <- processEntities(url)
        keywordsResponse <- processKeywords(url)
        webPageTextAnalysis <- processInfo(url, textResponse, entitiesResponse, keywordsResponse)
      } yield webPageTextAnalysis

      val futureSender = sender

      futureAnalysisResult map {
        analysisResult =>
          play.api.Logger.info("Returning analysis result from ..." + url)
          futureSender ! analysisResult
      }
    }
    /*case UrlInfo(url) => {
      logger.debug("Processing url: " + url)
      val result = for {
        textResponse <- processText(url)
        entitiesResponse <- processEntities(url)
        keywordsResponse <- processKeywords(url)
        webPageTextAnalysis <- processInfo(url, textResponse, entitiesResponse, keywordsResponse)
      } yield sender ! webPageTextAnalysis
    }*/
  }
}
