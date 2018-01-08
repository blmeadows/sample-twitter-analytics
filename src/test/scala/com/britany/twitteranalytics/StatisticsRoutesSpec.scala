package com.britany.twitteranalytics

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

class StatisticsRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with StatisticsRoutes {

  override val statisticsActor: ActorRef =
    system.actorOf(StatisticsActor.props, "statistics")

  lazy val routes = statisticsRoutes

  "StatisticsRoutes" should {
    "return empty results if none present (GET /statistics)" in {
      val request = HttpRequest(uri = "/statistics")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{
          |  "totalTweets":0,
          |  "topEmojis":{},
          |  "avgTweetPerSec":0,
          |  "urlPercent":"NaN",
          |  "topUrlDomains":{},
          |  "photoUrlPercent":"NaN",
          |  "topHashtags":{},
          |  "avgTweetPerMin":0,
          |  "emojiPercent":"NaN",
          |  "avgTweetPerHour":0
          | }""".stripMargin.replaceAll("\n( )+", ""))
      }
    }
  }
}
