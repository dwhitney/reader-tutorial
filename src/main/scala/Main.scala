package tutorial

import com.typesafe.config.ConfigFactory
import cats.data.Reader

import services._
import utils._

object Main extends App{


  val reader = for{
    server              <- Server.load
    port                <- ConfigReader.getInt("server.port")
    host                <- ConfigReader.getString("server.host")
    yahooFinanceService <- YahooFinanceService.load
  } yield (server, yahooFinanceService, port, host)

  val (server, yahooFinanceService, port, host) = reader.run(ConfigFactory.load)

  server.start(port,host)
  yahooFinanceService.pollAndUpdate

}
