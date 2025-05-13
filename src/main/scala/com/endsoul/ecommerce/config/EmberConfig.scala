package com.endsoul.ecommerce.config

import com.comcast.ip4s.{Host, Port}

import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

final case class EmberConfig(host: Host, port: Port) derives ConfigReader

object EmberConfig {

  given hostReader: ConfigReader[Host] = ConfigReader[String].emap { hostString =>
    Host
      .fromString(hostString)
      .toRight(
        // value, type, reason message
        CannotConvert(hostString, Host.getClass.toString, s"Invalid host string: $hostString")
      )
  }

  given portReader: ConfigReader[Port] = ConfigReader[Int].emap { portInt =>
    Port
      .fromInt(portInt)
      .toRight(
        // value, type, reason message
        CannotConvert(portInt.toString, Port.getClass.toString, s"Invalid port number: $portInt")
      )
  }
}
