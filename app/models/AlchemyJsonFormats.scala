package models

object AlchemyJsonFormats {
  import play.api.libs.json.Json

  implicit val analysisRequestFormat = Json.format[AnalysisRequest]
  implicit val entitiesFormat = Json.format[Entities]
  implicit val entitiesResponseFormat = Json.format[EntitiesResponse]
  implicit val keywordsFormat = Json.format[Keywords]
  implicit val keywordsResponseFormat = Json.format[KeywordsResponse]
  implicit val textResponseFormat = Json.format[TextResponse]
  implicit val webPageTextAnalysisFormat = Json.format[WebPageTextAnalysis]
  implicit val webPageTextAnalysisListFormat = Json.format[WebPageTextAnalysisList]
}