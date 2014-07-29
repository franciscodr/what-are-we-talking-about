package models

import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID

case class WebPageRequest(_id: BSONObjectID, requestDate: DateTime, url: String, webPageAnalysis: WebPageTextAnalysis)