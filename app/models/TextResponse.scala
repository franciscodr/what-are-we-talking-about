package models

case class TextResponse (status: String, statusInfo: Option[String] = None, usage: String, url: String, text: String) {}
