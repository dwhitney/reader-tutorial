package tutorial
package repository

import scala.concurrent.{ExecutionContext,Future}

import com.github.dwhjames.awswrap.dynamodb._

import models._

class YahooQuoteRepository(
  val readCapacity: Long
, val writeCapacity: Long
, val dynamodbClient: AmazonDynamoDBScalaClient
)(implicit
  val executionContext: ExecutionContext
, val serializer: DynamoDBSerializer[YahooQuote]
) extends DynamoDBRepostory[YahooQuote]

object YahooQuoteRepository{

  def yahooQuoteSerializer(yahooTableName: String): DynamoDBSerializer[YahooQuote] = {

    new DynamoDBSerializer[YahooQuote]{

      override val tableName: String = yahooTableName
      override val hashAttributeName: String = "symbol"

      override def primaryKeyOf(quote: YahooQuote) =
        Map("id" -> quote.symbol)

      override def toAttributeMap(quote: YahooQuote) =
        Map(
          "symbol" -> quote.symbol
        , "name" -> quote.name
        , "currency" -> quote.currency
        , "price" -> quote.price
        , "date" -> quote.date
        )

      override def fromAttributeMap(
        item: collection.mutable.Map[String, AttributeValue]) =
        YahooQuote(
          symbol =  item("symbol")
        , name =  item("name")
        , currency =  item("currency")
        , price =  item("price")
        , date =  item("date"))

    }

  }

}
