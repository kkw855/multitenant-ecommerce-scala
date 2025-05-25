package com.endsoul.ecommerce.modules

import cats.effect.{Async, Resource}

import doobie.util.ExecutionContexts
import doobie.hikari.HikariTransactor

import com.endsoul.ecommerce.config.PostgresConfig

object Database {
  def makePostgresResource[F[_]: Async](config: PostgresConfig): Resource[F, HikariTransactor[F]] = 
    for {
      ec <- ExecutionContexts.fixedThreadPool(config.nThreads)
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        config.url,
        config.user,
        config.pass,
        ec
      )
    } yield xa
}
