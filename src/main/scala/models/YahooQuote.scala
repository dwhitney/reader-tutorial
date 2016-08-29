package tutorial
package models

import io.circe.{Decoder,Encoder,HCursor,Json}
import io.circe.generic.semiauto._

case class YahooQuote(
  symbol: String
, name: String
, currency: String
, price: String
, date: String)

object YahooQuote{
  def decoder: Decoder[YahooQuote] = new Decoder[YahooQuote]{
    def apply(cursor: HCursor) = {
      for {
        query     <- cursor.downField("query").as[Json]
        results   <- query.hcursor.downField("results").as[Json]
        quote     <- results.hcursor.downField("quote").as[Json]
        symbol    <- quote.hcursor.downField("symbol").as[String]
        name      <- quote.hcursor.downField("Name").as[String]
        currency  <- quote.hcursor.downField("Currency").as[String]
        price     <- quote.hcursor.downField("LastTradePriceOnly").as[String]
        date      <- quote.hcursor.downField("LastTradeDate").as[String]
      } yield YahooQuote(symbol, name, currency, price, date)
    }
  }

  def encoder: Encoder[YahooQuote] = deriveEncoder[YahooQuote]
}
