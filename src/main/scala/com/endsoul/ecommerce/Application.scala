package com.endsoul.ecommerce

import cats.effect.{IO, IOApp}

import pureconfig.ConfigSource

import com.endsoul.ecommerce.config.AppConfig
import com.endsoul.ecommerce.config.syntax.*

object Application extends IOApp.Simple {
  // val config: Result[AppConfig] = ConfigSource.default.load[AppConfig]

  override def run: IO[Unit] = for {
    config <- ConfigSource.default.loadF[IO, AppConfig]
    _ <- config match {
      case AppConfig(postgresConfig, emberConfig) =>
        IO.println(s"Postgres config: $postgresConfig\nEmber config: $emberConfig")
    }
  } yield ()
}
