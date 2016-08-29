package tutorial
package utils

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.github.dwhjames.awswrap.dynamodb._
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import com.github.dwhjames.awswrap.sqs.AmazonSQSScalaClient

import com.typesafe.config.Config
import cats.data.Reader

object Misc{

  object AWSCredentials{

    def load: Reader[Config,BasicAWSCredentials] = for{
      awsAccessKeyId  <- ConfigReader.getString("aws.credentials.access-key-id")
      awsSecret       <- ConfigReader.getString("aws.credentials.secret-access-key")
    } yield new BasicAWSCredentials(awsAccessKeyId, awsSecret)

  }

  object DynamoDBClient{

    def load: Reader[Config,AmazonDynamoDBScalaClient] = for{
      credentials <- AWSCredentials.load
    } yield {
      new AmazonDynamoDBScalaClient(
        new AmazonDynamoDBAsyncClient(credentials)
      )
    }

  }

  object SQSClient{

    def load: Reader[Config,AmazonSQSScalaClient] = for{
      credentials     <- AWSCredentials.load
      actorComponents <- ActorComponents.load
    } yield {
      new AmazonSQSScalaClient(
        new AmazonSQSAsyncClient(credentials)
      , actorComponents.actorSystem.dispatcher
      )
    }

  }
}
