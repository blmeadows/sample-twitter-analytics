package com.britany.twitteranalytics

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.britany.twitteranalytics.StatisticsActor._
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import scala.concurrent.{ ExecutionContext, Future }
import scala.io.StdIn

object TwitterSampleStreamServer extends App with StatisticsRoutes {

  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val executionContext: ExecutionContext = system.dispatcher

  val statisticsActor: ActorRef = system.actorOf(StatisticsActor.props, "statisticsActor")

  // Make sure to define the following env variables:
  // TWITTER_CONSUMER_TOKEN_KEY and TWITTER_CONSUMER_TOKEN_SECRET
  // TWITTER_ACCESS_TOKEN_KEY and TWITTER_ACCESS_TOKEN_SECRET
  val streamingClient = TwitterStreamingClient()

  streamingClient.sampleStatuses(stall_warnings = true) {
    case tweet: Tweet => statisticsActor ! AnalyzeTweet(tweet)
  }

  lazy val routes: Route = statisticsRoutes

  val serverBindingFuture: Future[ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  StdIn.readLine()

  serverBindingFuture
    .flatMap(_.unbind())
    .onComplete { done =>
      done.failed.map { ex => log.error(ex, "Failed unbinding") }
      system.terminate()
    }
}
