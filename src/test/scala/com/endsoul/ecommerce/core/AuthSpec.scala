package com.endsoul.ecommerce.core

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.endsoul.ecommerce.security.{Argon2, Authenticator}
import com.endsoul.ecommerce.domain.auth.NewPasswordInfo
import com.endsoul.ecommerce.domain.user.*
import com.endsoul.ecommerce.fixtures.UserFixture
import pdi.jwt.JwtAlgorithm
import pdi.jwt.algorithms.JwtHmacAlgorithm

class AuthSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with UserFixture {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  
  //////////////////////////////////////////////////////////////////////////////////
  // prep
  //////////////////////////////////////////////////////////////////////////////////
  private val mockedUsers: Users[IO] = new Users[IO] {
    override def create(user: User): IO[String] = IO.pure(user.username)
    override def find(username: String): IO[Option[User]] =
      if (username == leeUsername) IO.pure(Some(Lee))
      else IO.pure(None)
    override def update(user: User): IO[Option[User]]  = IO.pure(Some(user))
    override def delete(username: String): IO[Boolean] = IO.pure(true)
  }

  val mockedAuthenticator: Authenticator[IO] = new Authenticator[IO] {
    override def encode(username: String): IO[String] = IO.pure("someToken")

    // TODO:
    override val key: String = "secretKey"
    override val algorithm: JwtHmacAlgorithm = JwtAlgorithm.HS256
  }

  "Auth 'algebra'" - {
    "signing up should not create a user with an existing username" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.signUp(
          NewUserInfo(
            leeUsername,
            "some@gmail.com",
            "somePassword"
          )
        )
      } yield maybeToken

      program.asserting(_ shouldBe None)
    }

    "signing up should create a completely new user" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeUser <- auth.signUp(
          NewUserInfo(
            "Bob",
            "bob@gmail.com",
            "somePassword"
          )
        )
      } yield maybeUser

      program.asserting(_ should matchPattern {
        case Some(
        User(
        "Bob",
        "bob@gmail.com",
        _
        )
        ) =>
      })
    }

    "login should return None if the user doesn't exist" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.login("user@gmail.com", "password")
      } yield maybeToken

      program.asserting(_ shouldBe None)
    }

    "login should return a token if the user exists but the password is wrong" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.login(leeUsername, "wrong-password")
      } yield maybeToken

      program.asserting(_ shouldBe None)
    }

    "login should return a token if the user exists and the password is correct" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        maybeToken <- auth.login(leeUsername, "lee123")
      } yield maybeToken

      program.asserting(_ shouldBe defined)
    }

    "changePassword should correctly change password if all details are correct" in {
      val program = for {
        auth <- LiveAuth[IO](mockedUsers, mockedAuthenticator)
        result <- auth.changePassword(leeUsername, NewPasswordInfo(leePassword, "scala-rocks"))
        isNicePassword <- result match {
          case Right(Some(user)) =>
            Argon2
              .checkPwBool[IO](
                "scala-rocks",
                user.storedHash
              )
          case _ => IO.pure(false)
        }
      } yield isNicePassword

      program.asserting(_ shouldBe true)
    }
  }
}
