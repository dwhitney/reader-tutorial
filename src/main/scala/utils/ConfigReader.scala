package tutorial
package utils

import com.typesafe.config.Config

import cats.data.Reader

object ConfigReader{

  def getString(name: String): Reader[Config,String] = Reader({ config =>
    config.getString(name)
  })
  
  def getInt(name: String): Reader[Config,Int] = Reader({ config =>
    config.getInt(name)
  })

}
