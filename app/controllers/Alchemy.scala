package controllers

import actors.alchemy.{PageTitleActor, PageTextActor, RankedKeywordsActor, RankedNamedEntitiesActor}
import actors.{UrlInfo, AlchemyActor}
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import org.joda.time.DateTime
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONObjectID
import scala.concurrent.duration._

import scala.concurrent.Future

class Alchemy extends Controller with MongoController {

  def collection: JSONCollection = db.collection[JSONCollection]("webPageRequestLog")

  private val alchemyActor = Akka.system.actorOf(Props[AlchemyActor], name = java.util.UUID.randomUUID().toString)
  private val pageTextActor = Akka.system.actorOf(Props[PageTextActor], name = java.util.UUID.randomUUID().toString)
  private val pageTitleActor = Akka.system.actorOf(Props[PageTitleActor], name = java.util.UUID.randomUUID().toString)
  private val rankedKeywordsActor = Akka.system.actorOf(Props[RankedKeywordsActor], name = java.util.UUID.randomUUID().toString)
  private val rankedNamedEntitiesActor = Akka.system.actorOf(Props[RankedNamedEntitiesActor], name = java.util.UUID.randomUUID().toString)

  implicit val timeout = Timeout(30 seconds)

  import models._
  import models.AlchemyJsonFormats._

  def getWebPageContentAnalysis(url: String): Future[WebPageTextAnalysis] = {
    val futureEntities = (rankedNamedEntitiesActor ? RequestInfo(url))
    val futureKeywords = (rankedKeywordsActor ? RequestInfo(url))
    val futureTitle = (pageTitleActor ? RequestInfo(url))
    val futureText = (pageTextActor ? RequestInfo(url))

    for {
      entities <- futureEntities.mapTo[Seq[Entities]]
      keywords <- futureKeywords.mapTo[Seq[Keywords]]
      text <- futureText.mapTo[String]
      title <- futureTitle.mapTo[String]
    } yield WebPageTextAnalysis(url,title, text,entities,keywords)
  }

  def saveWebPageRequest(webPageTextAnalysis: WebPageTextAnalysis) {
    val webPageRequest = WebPageRequest(BSONObjectID.generate, DateTime.now(),webPageTextAnalysis.url,webPageTextAnalysis)
    collection.insert(webPageRequest)
  }

  def analizeWebPage = Action.async(parse.json) {
    request =>
      request.body.validate[RequestInfo].map {
        requestInfo =>
          play.api.Logger.info("UrlList: " + requestInfo.url)

          val urlList = requestInfo.url split "," toSeq

          val forResult = for {
            url <- urlList
          } yield getWebPageContentAnalysis(url)

          val futureResult = Future.sequence(forResult)
          futureResult map {
            result =>
              for {
                webPageTextAnalysis <- result
              } yield saveWebPageRequest(webPageTextAnalysis)

              Ok(Json.toJson(WebPageTextAnalysisList(result)))
          }
      }.getOrElse(Future.successful(BadRequest("Invalid json")))
  }

  def getWebPageRequest = Action.async {

    val futureWebPageRequestList = collection.find(Json.obj()).sort(Json.obj("requestDate" -> -1)).cursor[WebPageRequest].collect[List]()

    val futureWebPageRequestJsonArray = futureWebPageRequestList map { webPageRequestList => Json.arr(webPageRequestList) }
    futureWebPageRequestJsonArray.map { webPageRequest => Ok(webPageRequest(0)) }
  }

  def getWebPageRequestDetail(id: String) = Action.async {

    val futureWebPageRequestList = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).sort(Json.obj("requestDate" -> -1)).cursor[WebPageRequest].collect[List]()

    val futureWebPageRequestJsonArray = futureWebPageRequestList map { webPageRequestList => Json.arr(webPageRequestList) }
    futureWebPageRequestJsonArray.map { webPageRequest => Ok(webPageRequest(0)) }
  }
}
