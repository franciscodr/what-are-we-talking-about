package models

object AlchemyJsonFormats {
  import play.api.libs.json.Json

  implicit val entitiesFormat = Json.format[Entities]
  implicit val entitiesResponseFormat = Json.format[EntitiesResponse]
  implicit val keywordsFormat = Json.format[Keywords]
  implicit val keywordsResponseFormat = Json.format[KeywordsResponse]
  implicit val requestInfoFormat = Json.format[RequestInfo]
  implicit val textResponseFormat = Json.format[TextResponse]
  implicit val titleResponseFormat = Json.format[TitleResponse]
  implicit val webPageTextAnalysisFormat = Json.format[WebPageTextAnalysis]
  implicit val webPageRequestFormat = Json.format[WebPageRequest]
  implicit val webPageTextAnalysisListFormat = Json.format[WebPageTextAnalysisList]
}