package tutorial
package utils

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

case class ActorComponents(
  actorSystem: ActorSystem
, materializer: ActorMaterializer
)

import cats.data.Reader
import com.typesafe.config.Config

object ActorComponents extends Memoizer[ActorComponents]{

  def reader: Reader[Config,ActorComponents] = for{
    actorSystemName <- ConfigReader.getString("actors.system_name")
  } yield {
    implicit val actorSystem = ActorSystem(actorSystemName)
    ActorComponents(
      actorSystem = actorSystem
    , materializer = ActorMaterializer()
    )
  }

}
