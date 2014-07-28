package controllers

import actors.{UrlInfo, AlchemyActor}
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.mvc._
import play.api.Play.current
import scala.concurrent.duration._

import scala.concurrent.Future

class Alchemy extends Controller {

  private val alchemyActor = Akka.system.actorOf(Props[AlchemyActor], name = java.util.UUID.randomUUID().toString)
  implicit val timeout = Timeout(10 seconds)

  import models._
  import models.AlchemyJsonFormats._

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
    WS.url("http://access.alchemyapi.com/calls/url/URLGetText").withQueryString(
      "apikey" -> "95986d748333af9be9ecc31705253e7a61142d58",
      "url" -> url,
      "outputMode" -> "json").get() map {
      response =>
        response.json.validate[TextResponse] match{
          case JsSuccess(textResponse, _) => textResponse
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

  def analizeWepPage = Action.async(parse.json) {
    request =>
      request.body.validate[AnalysisRequest].map {
        analysisRequest =>
          play.api.Logger.info("UrlList: " + analysisRequest.url)

          val responses = for {
            url <- analysisRequest.url split "," toSeq
          } yield {
            play.api.Logger.info("Getting info...")
            (alchemyActor ? UrlInfo(url)).mapTo[WebPageTextAnalysis]
          }

          val result = Future.sequence(responses)
          val futureWebPageTextAnalysisSeq = result map {
            wepPageTextAnalysisSeq =>
              play.api.Logger.info("Processing info...")
              wepPageTextAnalysisSeq
          }

          val futureValues = futureWebPageTextAnalysisSeq map { webPageTextAnalysisSeq => WebPageTextAnalysisList(webPageTextAnalysisSeq)}
          futureValues map { values => Ok(Json.toJson(values))}

      }.getOrElse(Future.successful(BadRequest("Invalid json")))
/*    request =>
      request.body.validate[AnalysisRequest].map {
        analysisRequest =>
          val result = for {
            textResponse <- processText(analysisRequest.url)
            entitiesResponse <- processEntities(analysisRequest.url)
            keywordsResponse <- processKeywords(analysisRequest.url)
            webPageTextAnalysis <- processInfo(analysisRequest.url, textResponse, entitiesResponse, keywordsResponse)
          } yield Ok(Json.toJson(webPageTextAnalysis))

          result recover {
            case e: RuntimeException => Conflict(e.getMessage())
          }
      }.getOrElse(Future.successful(BadRequest("Invalid json")))*/
  }

  def actors = Action.async(parse.json) {
    request =>
      request.body.validate[AnalysisRequest].map {
        analysisRequest =>
          play.api.Logger.info("UrlList: " + analysisRequest.url)

          val responses = for {
            url <- analysisRequest.url split "," toSeq
          } yield {
            play.api.Logger.info("Getting info...")
            (alchemyActor ? UrlInfo(url)).mapTo[WebPageTextAnalysis]
          }

          val result = Future.sequence(responses)
          val futureWebPageTextAnalysisSeq = result map {
            wepPageTextAnalysisSeq =>
              play.api.Logger.info("Processing info...")
              wepPageTextAnalysisSeq
          }

          val futureValues = futureWebPageTextAnalysisSeq map { webPageTextAnalysisSeq => WebPageTextAnalysisList(webPageTextAnalysisSeq)}
          futureValues map { values => Ok(Json.toJson(values))}

      }.getOrElse(Future.successful(BadRequest("Invalid json")))
  }
}
