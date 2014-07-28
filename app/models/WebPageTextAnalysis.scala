package models

case class WebPageTextAnalysisList(items : Seq[WebPageTextAnalysis]) {}

case class WebPageTextAnalysis(url: String, title: String, entities: Seq[Entities], keywords: Seq[Keywords]) {}
