package tutorial
package services

import scala.collection.JavaConversions._
import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.ActorMaterializer

import com.amazonaws.services.sqs.model._
import com.github.dwhjames.awswrap.sqs.AmazonSQSScalaClient

import models._

class YahooQueueService(
  sqsClient: AmazonSQSScalaClient
)(implicit
  actorSystem: ActorSystem
, materializer: ActorMaterializer

){

  import YahooQueueService._

  implicit val ec = actorSystem.dispatcher

  def createQueue(name: String): Future[URL] = {
    val request =
      new CreateQueueRequest(name)
      .withAttributes(Map("ReceiveMessageWaitTimeSeconds" -> "20"))
    sqsClient.createQueue(request).map{ result => result.getQueueUrl}
  }


  def enqueueSymbol(queue: URL, symbol: Symbol): Future[Unit] =
    sqsClient.sendMessage(queue, symbol).map{ result => println(result) }



  def enqueueSymbols(queue: URL, symbols: List[Symbol]): Future[Unit] =
    Source(symbols)
      .mapAsyncUnordered(8){ symbol => enqueueSymbol(queue, symbol) }
      .runWith(Sink.ignore)
      .map{ _ => () }

  def dequeue(queue: URL, num: Int): Future[Seq[(ReceiptHandle, Symbol)]] =
    sqsClient.receiveMessage(queue,num).map{ seq =>
      seq.map{ message => (message.getReceiptHandle, message.getBody) }
    }

  def deleteMessage(queue: URL, receipt: ReceiptHandle): Future[Unit] =
    sqsClient.deleteMessage(queue, receipt).map{ _ => () }

  def deleteMessages(queue: URL, receipts: List[ReceiptHandle]): Future[Unit] =
    Source(receipts)
      .mapAsyncUnordered(8){ receipt => deleteMessage(queue, receipt) }
      .runWith(Sink.ignore)
      .map{ _ => () }

}

import com.typesafe.config.Config
import cats.data.Reader
import utils._

object YahooQueueService extends Memoizer[YahooQueueService]{

  type URL = String
  type Symbol = String
  type ReceiptHandle = String

  def reader: Reader[Config,YahooQueueService] = for{
    sqsClient <- Misc.SQSClient.load
    actorComponents <- ActorComponents.load
  } yield {
    implicit val ActorComponents(actorSystem,materializer) = actorComponents
    new YahooQueueService(sqsClient)
  }

}
