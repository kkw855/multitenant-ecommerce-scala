package com.endsoul.ecommerce.modules

import cats.implicits.*
import cats.effect.{Concurrent, Resource}
import org.typelevel.log4cats.Logger
import org.http4s.{HttpRoutes, Uri}
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.headers.Origin

import scala.concurrent.duration.*
import com.endsoul.ecommerce.http.routes.{AuthRoutes, CategoryRoutes, HealthRoutes}

final class HttpApi[F[_]: {Concurrent, Logger}] private (core: Core[F]) {
  private val healthRoutes   = HealthRoutes[F].routes
  private val categoryRoutes = CategoryRoutes[F](core.categories, core.subcategories).routes
  private val authRoutes = AuthRoutes[F](core.auth).routes

  private val routes: HttpRoutes[F] = Router(
    "/api" -> (healthRoutes <+> categoryRoutes <+> authRoutes)
  )

  val endPoints: HttpRoutes[F] = CORS.policy
    .withAllowOriginHost(Set(
      Origin.Host(Uri.Scheme.http, Uri.RegName("localhost"), Some(3000)),
    ))
    .withAllowCredentials(true)
//    .withMaxAge(1.day)
    .apply(routes)
}

object HttpApi {
  def apply[F[_]: {Concurrent, Logger}](core: Core[F]): Resource[F, HttpApi[F]] =
    Resource.pure(new HttpApi[F](core))
}
