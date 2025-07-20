package com.endsoul.ecommerce.http.routes

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import org.http4s.implicits.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.endsoul.ecommerce.core.Auth
import com.endsoul.ecommerce.domain.auth.{LoginInfo, NewPasswordInfo}
import com.endsoul.ecommerce.domain.user.{NewUserInfo, User}
import com.endsoul.ecommerce.fixtures.UserFixture
import com.endsoul.ecommerce.security.Authenticator
import dev.profunktor.auth.jwt
import org.http4s.{HttpRoutes, Method, Request, Status}
import pdi.jwt.{JwtAlgorithm, JwtClaim}
import pdi.jwt.algorithms.JwtHmacAlgorithm

class AuthRouteSpec extends AsyncFreeSpec
  with AsyncIOSpec
  with Matchers
  with Http4sDsl[IO]
  with UserFixture {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  
  //////////////////////////////////////////////////////////////////////////////////
  // prep
  //////////////////////////////////////////////////////////////////////////////////
  val mockedAuthenticator: Authenticator[IO] = new Authenticator[IO] {
    // TODO:
    override val key: String = "secretKey"
    override val algorithm: JwtHmacAlgorithm = JwtAlgorithm.HS256
    override def encode(username: String): IO[String] = IO.pure("someToken")
  }

  val mockedAuth: Auth[IO] = new Auth[IO] {
    // TODO:
    override def authenticate: jwt.JwtToken => JwtClaim => IO[Option[User]] = _ => _ => IO.pure(Some(Lee))
    override def authenticator: Authenticator[IO] = mockedAuthenticator
    
    override def login(username: String, password: String): IO[Option[String]] =
      if (username == leeUsername && password == leePassword)
        mockedAuthenticator.encode(username).map(Some(_))
      else IO.pure(None)

    override def signUp(newUserInfo: NewUserInfo): IO[Option[User]] =
      if (newUserInfo.username === hongUsername)
        IO.pure(Some(Hong))
      else
        IO.pure(None)

    override def changePassword(username: String, newPasswordInfo: NewPasswordInfo): IO[Either[String, Option[User]]] =
      if (username == leeUsername)
        if (newPasswordInfo.oldPassword == leePassword)
          IO.pure(Right(Some(Lee)))
        else
          IO.pure(Left("Invalid password"))
      else
        IO.pure(Right(None))
  }

  val authRoutes: HttpRoutes[IO] = AuthRoutes[IO](mockedAuth).routes
  
  //////////////////////////////////////////////////////////////////////////////////
  // tests
  //////////////////////////////////////////////////////////////////////////////////
  "AuthRoutes" - {
    "should return a 400 - Bad Request if the user to create already exists" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/users")
            .withEntity(NewUserLee)
        )
      } yield {
        response.status shouldBe Status.BadRequest
      }
    }

    "should return a 201 - Created if the user creation succeeds" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/users")
            .withEntity(NewUserHong)
        )
      } yield {
        response.status shouldBe Status.Created
      }
    }

    "should return a 401 - unauthorized if login fails" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/login")
            .withEntity(LoginInfo(leeUsername, "wrong-password"))
        )
      } yield {
        response.status shouldBe Status.Unauthorized
      }
    }
  }
}
