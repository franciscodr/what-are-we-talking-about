package models

import org.joda.time.DateTime

case class WebPageRequest(requestDate: DateTime, url: String, webPageAnalysis: WebPageTextAnalysis) {}
