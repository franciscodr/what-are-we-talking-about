package models

case class WebPageTextAnalysis(url: String, title: String, entities: Seq[Entities], keywords: Seq[Keywords]) {}
