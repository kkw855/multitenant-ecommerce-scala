package com.endsoul.ecommerce.core


import cats.implicits.*
import cats.effect.kernel.Async
import cats.implicits.catsSyntaxApplicativeId
import com.endsoul.ecommerce.domain.auth.{NewPasswordInfo, TokenPayLoad}
import dev.profunktor.auth.jwt.JwtToken
import pdi.jwt.{JwtClaim}
import doobie.postgres.implicits._
import com.endsoul.ecommerce.domain.user.{NewUserInfo, User}
import com.endsoul.ecommerce.security.{Argon2, Authenticator}

import org.typelevel.log4cats.Logger
import io.circe.generic.auto.*
import io.circe.parser.*

trait Auth[F[_]] {
  def authenticator: Authenticator[F]
  def authenticate: JwtToken => JwtClaim => F[Option[User]]

  def signUp(newUserInfo: NewUserInfo): F[Option[(String, String)]]
  def login(username: String, password: String): F[Option[(String, String)]]
  def refreshToken(token: String): F[Option[(String, String)]]
  def changePassword(
      email: String,
      newPasswordInfo: NewPasswordInfo
  ): F[Either[String, Option[User]]]
}

class LiveAuth[F[_]: {Async, Logger}] private (
    users: Users[F],
    override val authenticator: Authenticator[F]
) extends Auth[F] {

  override def authenticate: JwtToken => JwtClaim => F[Option[User]] =
    (token: JwtToken) =>
      (claim: JwtClaim) => {
        decode[TokenPayLoad](claim.content) match {
          case Right(payload) => users.find(payload.username)
          case Left(_)        => None.pure[F]
        }
      }

  override def signUp(newUserInfo: NewUserInfo): F[Option[(String, String)]] = {
    users.find(newUserInfo.username).flatMap {
      case Some(_) => None.pure[F]
      case None =>
        for {
          storedHash <- Argon2.hashPw[F](newUserInfo.password)
          user       <- User(newUserInfo.username, newUserInfo.email, storedHash, None).pure[F]
          _          <- users.create(user)
          maybeJwtToken <- authenticator.encode(user)
        } yield Some(maybeJwtToken)
    }
  }

  override def login(username: String, password: String): F[Option[(String, String)]] =
    for {
      maybeUser <- users.find(username)
      // Option[User].filter(User => IO[Boolean]) => IO[Option[User]]
      maybeValidateUser <- maybeUser.filterA(user =>
        Argon2.checkPwBool[F](password, user.storedHash)
      )
      maybeJwtToken <- maybeValidateUser.traverse(user => authenticator.encode(user))
    } yield maybeJwtToken

  def refreshToken(token: String): F[Option[(String, String)]] =
    for {
      decoded <- authenticator.decodeToken(token)
      maybeUser <- decoded match {
        case Some(value) => users.find(value.username)
        case None => None.pure[F]
      }
      maybeJwtToken <- maybeUser.traverse(user => authenticator.encode(user))
    } yield maybeJwtToken

  override def changePassword(
      email: String,
      newPasswordInfo: NewPasswordInfo
  ): F[Either[String, Option[User]]] = {

    def updateUser(user: User, newPassword: String): F[Option[User]] =
      for {
        storedHash  <- Argon2.hashPw[F](newPassword)
        updatedUser <- users.update(user.copy(storedHash = storedHash))
      } yield updatedUser

    def checkAndUpdate(
        user: User,
        oldPassword: String,
        newPassword: String
    ): F[Either[String, Option[User]]] =
      for {
        passCheck <- Argon2
          .checkPwBool[F](oldPassword, user.storedHash)
        updateResult <-
          if (passCheck) updateUser(user, newPassword).map(Right(_))
          else Left("Invalid password").pure[F]
      } yield updateResult

    users.find(email).flatMap {
      case None => Right(None).pure[F]
      case Some(user) =>
        val NewPasswordInfo(oldPassword, newPassword) = newPasswordInfo
        checkAndUpdate(user, oldPassword, newPassword)
    }
  }
}

object LiveAuth {
  def apply[F[_]: {Async, Logger}](
      users: Users[F],
      authenticator: Authenticator[F]
  ): F[LiveAuth[F]] =
    new LiveAuth[F](users, authenticator).pure[F]
}
