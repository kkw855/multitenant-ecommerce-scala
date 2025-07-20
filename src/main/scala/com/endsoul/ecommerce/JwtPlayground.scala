package com.endsoul.ecommerce

import cats.effect.*
import cats.implicits.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.server.*
import org.http4s.implicits.*
import org.http4s.ember.server.*
import com.comcast.ip4s.*
import dev.profunktor.auth.jwt.{JwtAuth, JwtToken}
import dev.profunktor.auth.JwtAuthMiddleware
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

import java.time.Instant

case class TokenPayLoad(user: String, level: String)
case class AuthUser(id: Long, name: String)
val database = Map("John" -> AuthUser(123, "JohnDoe"))

object JwtPlayground extends IOApp.Simple {
  val key = "secretKey"
  val algo = JwtAlgorithm.HS256
  
  val authenticate: JwtToken => JwtClaim => IO[Option[AuthUser]] =
    (token: JwtToken) =>
      (claim: JwtClaim)
      => decode[TokenPayLoad](claim.content) match {
        case Right(payload) =>
          IO(database.get(payload.user))
        case Left(_) => IO(None)
      }

  val jwtAuth = JwtAuth.hmac(key, algo)
  val middleware = JwtAuthMiddleware[IO, AuthUser](jwtAuth, authenticate)

  val authedRoutes: AuthedRoutes[AuthUser, IO] =
    AuthedRoutes.of {
      case GET -> Root / "welcome" as user =>
        Ok(s"Welcome, ${user.name}")
    }

  val securedRoutes: HttpRoutes[IO] = middleware(authedRoutes)

  val now = Instant.now
  println(now.getEpochSecond)
  println(now.plusSeconds(123).getEpochSecond)
  val json = TokenPayLoad("John", "basic").asJson.noSpaces
  println(json)
  val decodedJson = decode[TokenPayLoad](json)
  println(decodedJson)
  val claim = JwtClaim(
    content = """{"user":"John", "level":"basic"}""",
    expiration = Some(Instant.now.plusSeconds(157784760).getEpochSecond),
    issuedAt = Some(Instant.now.getEpochSecond)
  )

  val token = JwtCirce.encode(claim, key, algo)
  println(token)
  val decoded = JwtCirce.decode(token, key, Seq(algo))
  println(decoded)
  decoded.map(s => println(s.content))
  val decoded2 = JwtCirce.decodeJson(token, key, Seq(algo))
  println(decoded2)
  println()
  
  val loginRoutes: HttpRoutes[IO] =
    HttpRoutes.of { case GET -> Root / "login" =>
      Ok("Logged In").map(
        _.addCookie(ResponseCookie(name = "token", content = token, httpOnly = true, secure = true))
      )
    }

  val service = loginRoutes <+> securedRoutes

  val server: Resource[IO, Server] = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"8080")
    .withHttpApp(service.orNotFound)
    .build

  override def run: IO[Unit] = server.use(_ => IO.never).as(ExitCode.Success)
}
