package models

case class EntitiesResponse(status: String, statusInfo: Option[String] = None, usage: String, url: String, language: String, entities: Option[Seq[Entities]] = None) {}

case class Entities(`type`: String, relevance: String, count: String, text: String)