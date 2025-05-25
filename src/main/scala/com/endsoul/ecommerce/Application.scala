package com.endsoul.ecommerce

import cats.effect.{IO, IOApp}

import pureconfig.ConfigSource

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*

import org.http4s.ember.server.EmberServerBuilder

import java.util.UUID

import com.endsoul.ecommerce.config.syntax.*
import com.endsoul.ecommerce.config.AppConfig
import com.endsoul.ecommerce.domain.category.{CategoryInfo, Category}
import com.endsoul.ecommerce.modules.{Core, Database, HttpApi}

object Application extends IOApp.Simple {
  private val rawJson: String =
    """
  {
    "id": "acdcd251-9001-41ae-adb4-ad601c171cea",
    "name": "bar",
    "slug": "123",
    "color": "#123456"
  }
  """

  println(CategoryInfo(UUID.fromString("acdcd251-9001-41ae-adb4-ad601c171cea"), "a", "b", "c").asJson)
  private val otherJson = parser.decode[Category](rawJson)
  println(otherJson)

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = ConfigSource.default.loadF[IO, AppConfig].flatMap {
    // Transactor -> Core(Categories) -> HttpApi(HealthRoutes, CategoryRoutes)
    case AppConfig(postgresConfig, emberConfig) =>
      val appResource = for {
        xa <- Database.makePostgresResource[IO](postgresConfig)
        core <- Core[IO](xa)
        httpApi <- HttpApi[IO](core)
        server <- EmberServerBuilder
          .default[IO]
          .withHost(emberConfig.host)
          .withPort(emberConfig.port)
          .withHttpApp(httpApi.endPoints.orNotFound)
          .build
      } yield server

      appResource.use(_ => IO.println("Start Ecommerce Server") *> IO.never)
  }

//  val resource1 = Resource.make[IO, Int](IO.println("acquire Resource 1") *> IO.pure(1))(_ => IO.println("release Resource 1"))
//  val resource2 = Resource.make[IO, Int](IO.println("acquire Resource 2") *> IO.pure(2))(_ => IO.println("release Resource 2"))
//  val res1: IO[Unit] = resource1.use(_ => IO.println("use Resource 1"))
//  override def run: IO[Unit] = for {
//    _ <- IO.pure(0)
//    res1 <- resource1.use(_ => IO.println("use Resource 1"))
//    res2 <- resource2
//  } yield ()
//  val flat = resource1.flatMap(res1 => resource2.map(res2 => res1 + res2))
//
//  override def run: IO[Unit] = for {
//    _ <- IO.pure(0)
//    res <- flat.use( res => IO.pure(res * 10))
//    _ <- IO.println(res)
//  } yield ()
}
