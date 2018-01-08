package com.britany.twitteranalytics

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val statisticsJsonFormat: RootJsonFormat[TweetStatistics] = jsonFormat10(TweetStatistics)

}
