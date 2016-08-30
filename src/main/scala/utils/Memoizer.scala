package tutorial
package utils

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap}

import com.typesafe.config.Config

import cats.data.Reader

object Memoizer{
  private val memoized: MutableMap[Config,MutableMap[Any,Any]] = MutableMap()
}

trait Memoizer[T]{ self =>

  def reader : Reader[Config,T]

  def load: Reader[Config,T] = memoize(reader)

  def memoize(r: Reader[Config,T]): Reader[Config,T] = Memoizer.memoized.synchronized{
    Reader({ config: Config =>

      if(!Memoizer.memoized.get(config).isDefined) Memoizer.memoized.put(config,MutableMap())

      Memoizer.memoized(config).get(self) match {
        case None =>
          val value = r.run(config)
          Memoizer.memoized(config).put(self,value)
          value
        case Some(value) =>
          value.asInstanceOf[T]
      }

    })
  }

}
