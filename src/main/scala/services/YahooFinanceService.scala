package tutorial
package services

import scala.concurrent.{Await,Future}
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl._
import akka.stream.ActorMaterializer

import io.circe.jawn.parse

import com.typesafe.config._
import cats.data.{Reader,Xor}

import models._
import repository._
import utils._

class YahooFinanceService(
  yahooQuoteRepository: YahooQuoteRepository
, yahooQueueService: YahooQueueService
, yahooQueueURL: String
, quoteURL: String = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22:::SYMBOL:::%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback="
)(implicit
  actorSystem: ActorSystem
, materializer: ActorMaterializer
){

  import YahooQueueService._

  implicit val ec = actorSystem.dispatcher

  def getQuoteFromYahoo(symbol: String): Future[Xor[Throwable,YahooQuote]] = {
    val url = quoteURL.replaceAll(":::SYMBOL:::", symbol)
    Http().singleRequest(HttpRequest(uri = url))
      .map(_.entity.dataBytes.map(_.decodeString("UTF-8")))
      .flatMap(_.runWith(Sink.head))
      .map(parse(_).flatMap{ json => YahooQuote.decoder(json.hcursor)})
  }

  def getQuote(symbol: String): Future[Option[YahooQuote]] = {
    yahooQuoteRepository.get(symbol).flatMap{
      case None => yahooQueueService.enqueueSymbol(yahooQueueURL, symbol).map{ _ => None }
      case quoteOpt => Future.successful(quoteOpt)
    }
  }

  def pollAndUpdate = {
    println(s"Polling $yahooQueueURL")
    while(true){
      val symbols = Await.result(yahooQueueService.dequeue(yahooQueueURL,1), 25.seconds).toList
      symbols.foreach{
        case (receiptHandle,symbol) =>
          println(s"processing request for $symbol")
          getQuoteFromYahoo(symbol)
          .flatMap{
            case Xor.Right(quote) => yahooQuoteRepository.insert(quote)
            case Xor.Left(_) => Future.successful(())
          }
          .flatMap{ _ => yahooQueueService.deleteMessage(yahooQueueURL, receiptHandle)}
          .recoverWith{
            case error => yahooQueueService.deleteMessage(yahooQueueURL, receiptHandle)
          }
      }
    }
  }

}


object YahooFinanceService{

  def load: Reader[Config,YahooFinanceService] = for{
    yahooQuoteRepository <- YahooQuoteRepository.load
    yahooQueueService    <- YahooQueueService.load
    yahooQuoteQueueName  <- ConfigReader.getString("aws.sqs.yahoo-quote-queue-name")
    actorComponents      <- ActorComponents.load
  } yield {
    implicit val ActorComponents(actorSystem, materializer) = actorComponents
    val url = Await.result(yahooQueueService.createQueue(yahooQuoteQueueName), 25.seconds)
    new YahooFinanceService(
      yahooQuoteRepository
    , yahooQueueService
    , url
    )
  }

}
