package models

case class TitleResponse (status: String, statusInfo: Option[String] = None, usage: String, url: String, title: String) {}
