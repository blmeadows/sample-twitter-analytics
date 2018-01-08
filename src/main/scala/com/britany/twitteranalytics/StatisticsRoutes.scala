package com.britany.twitteranalytics

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.britany.twitteranalytics.StatisticsActor._
import scala.concurrent.Future
import scala.concurrent.duration._

trait StatisticsRoutes extends JsonSupport {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[StatisticsRoutes])

  def statisticsActor: ActorRef

  implicit lazy val timeout: Timeout = Timeout(5.seconds)

  lazy val statisticsRoutes: Route =
    pathPrefix("statistics") {
      concat(
        pathEnd {
          concat(
            get {
              val tweetStatistics: Future[TweetStatistics] =
                (statisticsActor ? GetTweetAnalytics).mapTo[TweetStatistics]
              complete(tweetStatistics)
            }
          )
        }
      )
    }
}
