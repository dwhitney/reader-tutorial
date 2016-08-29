package tutorial
package utils

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

case class ActorComponents(
  actorSystem: ActorSystem
, materializer: ActorMaterializer
)
