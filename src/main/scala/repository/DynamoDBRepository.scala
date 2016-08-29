package tutorial
package repository

import scala.concurrent.{Await,ExecutionContext,Future}
import scala.concurrent.duration._

import com.amazonaws.services.dynamodbv2.model._

import com.github.dwhjames.awswrap.dynamodb._


trait DynamoDBRepostory[T]{

  val readCapacity: Long
  val writeCapacity: Long
  val dynamodbClient: AmazonDynamoDBScalaClient

  implicit val serializer: DynamoDBSerializer[T]
  implicit val executionContext: ExecutionContext

  val mapper = AmazonDynamoDBScalaMapper(dynamodbClient)


  protected def tableRequest =
    new CreateTableRequest()
      .withTableName(serializer.tableName)
      .withProvisionedThroughput(
        Schema.provisionedThroughput(readCapacity,writeCapacity))
      .withAttributeDefinitions(
        Schema.stringAttribute(serializer.hashAttributeName))
      .withKeySchema(
        Schema.hashKey(serializer.hashAttributeName))

  protected def awaitTableCreation(tableName: String): Unit = {
      println(s"Waiting for $tableName table to become active.")
      val deadline = 10.minutes.fromNow
      while (deadline.hasTimeLeft) {
        val result = Await.result(
          dynamodbClient.describeTable(tableName),
          10.seconds
        )
        val description = result.getTable
        if (description.getTableStatus == TableStatus.ACTIVE.toString)
          return println("Table creation complete")
        Thread.sleep(20 * 1000)
      }
      throw new RuntimeException(s"Timed out waiting for $tableName table to become active.")
    }

  def createTable: Unit = try{
    Await.result(dynamodbClient.describeTable(serializer.tableName), 20.seconds)
  } catch {
    case e: ResourceNotFoundException =>
      Await.result(dynamodbClient.createTable(tableRequest).map{ _ => () }, 20.seconds)
      awaitTableCreation(serializer.tableName)
  }

  def insert(value: T): Future[Unit] = mapper.dump[T](value)

  def get(id: String): Future[Option[T]] = {
    try{
      mapper.loadByKey[T](id)
    } catch {
      case e: ResourceNotFoundException => Future.successful(None)
    }
  }

  def delete(id: String): Future[Unit] =
    mapper.deleteByKey[T](id).map{ _ => ()}


}
