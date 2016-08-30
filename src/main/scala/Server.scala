package tutorial

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.stream.scaladsl._
import akka.stream.ActorMaterializer

import models._
import services._

class Server(
  yahooFinanceService: YahooFinanceService
)(implicit
  actorSystem: ActorSystem
, materializer: ActorMaterializer
){

  implicit val ec = actorSystem.dispatcher

  val route = {
    path("price" / Segment){ symbol =>
      get{
        complete(
          yahooFinanceService
            .getQuote(symbol)
            .map{
              case Some(symbol) =>
                HttpEntity(ContentTypes.`application/json`, YahooQuote.encoder(symbol).spaces4)
              case None =>
                HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Sorry no price found for $symbol. Please Try again in a moment</h1>")
            }
        )
      }
    }
  }

  def start(port: Int, host: String) =
    Http().bindAndHandle(route, host, port)

}

import com.typesafe.config._
import cats.data.Reader
import utils._

object Server extends Memoizer[Server]{

  def reader: Reader[Config,Server] = for{
    yahooFinanceService <- YahooFinanceService.load
    actorComponents     <- ActorComponents.load
  } yield {
    implicit val ActorComponents(actorSystem, materializer) = actorComponents
    new Server(yahooFinanceService)
  }

}
