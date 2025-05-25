package com.endsoul.ecommerce.core

import cats.effect.{IO, Resource}

import doobie.{ExecutionContexts, Transactor}
import doobie.hikari.HikariTransactor

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

trait DoobieSpec {
  val initScript: String

  val postgres: Resource[IO, PostgreSQLContainer[Nothing]] = {
    val acquire = IO {
      val container = new PostgreSQLContainer(DockerImageName.parse("postgres:17.5-alpine"))
      container.withInitScript(initScript)
      container.start()
      println("Container started")
      container
    }

    val release = (container: PostgreSQLContainer[Nothing]) =>
      IO.println("Container stopped") >> IO(container.stop())

    Resource.make(acquire)(release)
  }

  val transactor: Resource[IO, Transactor[IO]] = for {
    db <- postgres
    ec <- ExecutionContexts.fixedThreadPool[IO](1)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      db.getJdbcUrl,
      db.getUsername,
      db.getPassword,
      ec
    )
  } yield xa
}
