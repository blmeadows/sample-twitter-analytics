package com.britany.twitteranalytics

import akka.actor.{ Actor, ActorLogging, Props }
import com.danielasfregola.twitter4s.entities.Tweet
import java.net.URL
import java.util.Calendar
import com.vdurmont.emoji.EmojiParser
import scala.collection.JavaConverters._

final case class TweetStatistics(
  totalTweets: Long, // Total number of tweets received
  avgTweetPerHour: Long, // Average tweets per hour
  avgTweetPerMin: Long, // Average tweets per minute
  avgTweetPerSec: Long, // Average tweets per second
  topEmojis: Map[String, Int], // Top emojis in tweets
  emojiPercent: String, // Percent of tweets that contains emojis
  topHashtags: Map[String, Int], // Top hashtags
  urlPercent: String, // Percent of tweets that contain a url
  photoUrlPercent: String, // Percent of tweets that contain a photo url (pic.twitter.com or instagram)
  topUrlDomains: Map[String, Int] // Top domains of urls in tweets
)

object StatisticsActor {

  final case class AnalyzeTweet(tweet: Tweet)
  final case object GetTweetAnalytics

  def props: Props = Props[StatisticsActor]
}

class StatisticsActor extends Actor with ActorLogging {
  import StatisticsActor._

  private val cal = Calendar.getInstance()
  private val streamingStart = cal.getTimeInMillis / 1000
  private var totalTweets = 0
  private var totalTweetsWithEmojis = 0
  private var totalTweetsWithUrl = 0
  private var totalTweetWithPhotoUrl = 0
  private var allEmojis: Map[String, Int] = Map.empty
  private var allHashtags: Map[String, Int] = Map.empty
  private var allUrlDomains: Map[String, Int] = Map.empty

  def tweetAnalysis(tweet: Tweet): Unit = {
    var photo = false
    var emoji = false
    tweet.entities.foreach { entity =>
      entity.urls.foreach { url =>
        val domain = new URL(url.expanded_url).getHost.toLowerCase
        if (domain == "instagram" && !photo) {
          photo = true
          totalTweetWithPhotoUrl += 1
        } else {
          val count = allUrlDomains.getOrElse(domain, 0)
          allUrlDomains += domain -> (count + 1)
          totalTweetsWithUrl += 1
        }
      }
      if (!photo && entity.media.nonEmpty) {
        photo = true
        totalTweetWithPhotoUrl += 1
      }
      entity.hashtags.foreach { hashtag =>
        val ht = hashtag.text.toLowerCase
        val count = allHashtags.getOrElse(ht, 0)
        allHashtags += ht -> (count + 1)
      }
    }
    val emojis = EmojiParser.extractEmojis(tweet.text).asScala
    emojis.foreach { e =>
      val count = allEmojis.getOrElse(e, 0)
      allEmojis += e -> (count + 1)
      if (!emoji) {
        totalTweetsWithEmojis += 1
      } else {
        emoji = true
      }
    }
  }

  def calculatePercent(total: Int): String = {
    f"${total.toFloat / totalTweets}%1.2f"
  }

  def receive: Receive = {
    case AnalyzeTweet(tweet) =>
      totalTweets += 1
      tweetAnalysis(tweet)

    case GetTweetAnalytics =>
      val currentTime = Calendar.getInstance().getTimeInMillis / 1000
      val tweetsPerSecond = totalTweets / (currentTime - streamingStart)
      val topEmojis = allEmojis.toList.sortBy(_._2).takeRight(5).reverse.toMap
      val topHashtags = allHashtags.toList.sortBy(_._2).takeRight(5).reverse.toMap
      val topUrlDomains = allUrlDomains.toList.sortBy(_._2).takeRight(5).reverse.toMap

      val statistics = TweetStatistics(
        totalTweets,
        tweetsPerSecond * 60 * 60, // per hour
        tweetsPerSecond * 60, // per minute
        tweetsPerSecond, // per second
        topEmojis,
        calculatePercent(totalTweetsWithEmojis),
        topHashtags,
        calculatePercent(totalTweetsWithUrl),
        calculatePercent(totalTweetWithPhotoUrl),
        topUrlDomains
      )
      sender() ! statistics

  }
}
