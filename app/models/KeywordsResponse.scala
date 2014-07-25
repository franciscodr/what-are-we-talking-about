package models

case class KeywordsResponse(status: String, statusInfo: Option[String] = None, usage: String, url: String, language: String, keywords: Option[Seq[Keywords]] = None) {}

case class Keywords(text: String, relevance: String)
