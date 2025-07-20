package com.endsoul.ecommerce.http.routes

import cats.effect.{Concurrent, IO}
import org.http4s.{AuthedRoutes, HttpRoutes, Response, ResponseCookie, Status}
import cats.implicits.*
import com.endsoul.ecommerce.{AuthUser, TokenPayLoad}
import com.endsoul.ecommerce.JwtPlayground.token
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.http4s.circe.CirceEntityCodec.*
import org.typelevel.log4cats.Logger
import com.endsoul.ecommerce.core.Auth
import com.endsoul.ecommerce.domain.auth.{LoginInfo, Token}
import com.endsoul.ecommerce.domain.user.{NewUserInfo, User}
import dev.profunktor.auth.JwtAuthMiddleware
import dev.profunktor.auth.jwt.{JwtAuth, JwtToken}
import io.circe.parser.decode
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.Ok
import org.http4s.headers.Cookie
import org.http4s.server.AuthMiddleware
import pdi.jwt.JwtClaim
//import com.endsoul.ecommerce.http.validation.syntax.HttpValidationDsl
import org.http4s.server.Router

class AuthRoutes[F[_]: {Concurrent, Logger}] private (auth: Auth[F]) extends Http4sDsl[F] {

  private val authenticator               = auth.authenticator
  private val jwtAuth                     = JwtAuth.hmac(authenticator.key, authenticator.algorithm)
  val middleware: AuthMiddleware[F, User] = JwtAuthMiddleware[F, User](jwtAuth, auth.authenticate)

  // TODO: validation 추가
  // POST /auth/users { NewUserInfo } => 201 Created or BadRequest
  private val createUserRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "users" =>
      for {
        newUserInfo   <- req.as[NewUserInfo]
        maybeJwtToken <- auth.signUp(newUserInfo)
        resp <- maybeJwtToken match {
          case Some((accessToken, refreshToken)) =>
            Created(Token(accessToken)).map(
              _.addCookie(
                ResponseCookie(
                  name = "funroad-token",
                  content = refreshToken,
                  httpOnly = true,
                  secure = false,
                  path = Some("/")
                )
              )
            )
          case None => BadRequest(s"Username ${newUserInfo.username} already exists.")
        }
      } yield resp
  }

  // TODO: validation 추가
  // POST /auth/login { LoginInfo } => 200 OK with Authorization: Bearer {jwt}
  private val loginRoute: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "login" =>
    val maybeJwtToken = for {
      loginInfo  <- req.as[LoginInfo]
      maybeToken <- auth.login(loginInfo.username, loginInfo.password)
      _          <- Logger[F].info(s"User logging in: ${loginInfo.username}")
    } yield maybeToken

    maybeJwtToken.map {
      case Some((accessToken, refreshToken)) =>
        Response(Status.Ok)
          .withEntity(Token(accessToken))
          .addCookie(
            ResponseCookie(
              name = "funroad-token",
              content = refreshToken,
              httpOnly = true,
              secure = false,
              path = Some("/")
            )
          )
      case None => Response(Status.Unauthorized)
    }
  }

  // GET /auth/logout
  private val logoutRoute: AuthedRoutes[User, F] = AuthedRoutes.of {
    case GET -> Root / "logout" as user =>
      for {
        _ <- Logger[F].info(s"User logging out: ${user.username}")
        resp <- Ok("Logged out").map(
          _.removeCookie(
            ResponseCookie(
              name = "funroad-token",
              content = "",
              httpOnly = true,
              secure = false,
              path = Some("/")
            )
          )
        )
      } yield resp
  }

  // GET /auth/refreshToken => 200 OK with Authorization: Bearer {jwt}
  private val refreshTokenRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ GET -> Root / "refreshToken" =>
      println(s"refreshTokenRoute $req.headers")
      val refreshToken = req.headers.get[Cookie].flatMap { cookieHeader =>
        cookieHeader.values.find(_.name == "funroad-token").map(_.content)
      }

      println(s"refreshTokenRoute 2 $refreshToken")

      refreshToken match {
        case Some(token) =>
          for {
            maybeJwtToken <- auth.refreshToken(token)
            _             <- Logger[F].info(s"RefreshToken: $maybeJwtToken")
            resp <- maybeJwtToken match {
              case Some((accessToken, _)) => Ok(Token(accessToken))
              case None                   => BadRequest()
            }
          } yield resp
        case None => BadRequest()
      }
  }

  private val authedRoutes: AuthedRoutes[User, F] =
    AuthedRoutes.of { case GET -> Root / "welcome" as user =>
      Ok(s"Welcome, ${user.username}")
    }

  private val securedRoutes: HttpRoutes[F] = middleware(authedRoutes <+> logoutRoute)

  val routes: HttpRoutes[F] = Router(
    "/auth" -> (createUserRoute <+> loginRoute <+> refreshTokenRoute <+> securedRoutes)
  )
}

object AuthRoutes {
  def apply[F[_]: {Concurrent, Logger}](auth: Auth[F]) = new AuthRoutes[F](auth)
}
