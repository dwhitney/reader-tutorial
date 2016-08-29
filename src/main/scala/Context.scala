package tutorial

import java.util.concurrent.Executors

import scala.concurrent.{Await,Future}
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import com.typesafe.config._

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import com.github.dwhjames.awswrap.sqs.AmazonSQSScalaClient

import com.github.dwhjames.awswrap.dynamodb._

import models._
import services._
import repository._


object Context{

  /****** CONFIG VARIABLES *********/

  //actors
  val config = ConfigFactory.load
  val actorSystemName = config.getString("actors.system_name")

  //aws creds
  val awsAccessKeyId = config.getString("aws.credentials.access-key-id")
  val awsSecret = config.getString("aws.credentials.secret-access-key")

  //db
  val yahooReadCapacity = config.getInt("aws.dynamodb.yahoo-quote.read-capacity")
  val yahooWriteCapacity = config.getInt("aws.dynamodb.yahoo-quote.write-capacity")
  val yahooTableName = config.getString("aws.dynamodb.yahoo-quote.table-name")

  //sqs
  val yahooQuoteQueueName = config.getString("aws.sqs.yahoo-quote-queue-name")

  //server
  val port = config.getInt("server.port")
  val host = config.getString("server.host")


  /******* Construction **********/

  //misc exec environment
  implicit val actorSystem = ActorSystem(actorSystemName)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher


  //aws
  val basicAwsCredentials = new BasicAWSCredentials(awsAccessKeyId, awsSecret)

  //aws dynamodb
  val jdkDynamodbClient = new AmazonDynamoDBAsyncClient(basicAwsCredentials)
  val scalaDynamoDBClient = new AmazonDynamoDBScalaClient(jdkDynamodbClient)

  implicit val yahooQuoteDynamoDserializer =
    YahooQuoteRepository.yahooQuoteSerializer(yahooTableName)

  val yahooQuoteRepository = new YahooQuoteRepository(yahooReadCapacity, yahooWriteCapacity, scalaDynamoDBClient)

  //aws sqs
  val sqsClient = new AmazonSQSAsyncClient(basicAwsCredentials)
  val sqsScalaClient = new AmazonSQSScalaClient(sqsClient, executionContext)

  val yahooQueueService = new YahooQueueService(sqsScalaClient)

  val yahooQueueURL = Await.result(yahooQueueService.createQueue(yahooQuoteQueueName), 25.seconds)

  //service construction
  val yahooFinanceService = new YahooFinanceService(
      yahooQuoteRepository
    , yahooQueueService
    , yahooQueueURL
    )


  //web server
  val server = new Server(yahooFinanceService)

}
