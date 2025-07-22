package com.endsoul.ecommerce.core

import cats.implicits.*
import cats.effect.kernel.Async

import dev.profunktor.auth.jwt.JwtToken
import pdi.jwt.JwtClaim

import io.circe.generic.auto.*
import io.circe.parser.*

import org.typelevel.log4cats.Logger

import com.endsoul.ecommerce.domain.user.*
import com.endsoul.ecommerce.domain.auth.*
import com.endsoul.ecommerce.security.{Argon2, Authenticator}

trait Auth[F[_]] {
  def authenticator: Authenticator[F]
  def authenticate: JwtToken => JwtClaim => F[Option[User]]

  def signUp(newUserInfo: NewUserInfo): F[Option[AuthToken]]
  def login(username: String, password: String): F[Option[AuthToken]]
  def refreshAccessToken(token: String): F[Option[String]]
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
        decode[AccessTokenPayLoad](claim.content) match {
          case Right(payload) => users.find(payload.username)
          case Left(_)        => None.pure[F]
        }
      }

  override def signUp(newUserInfo: NewUserInfo): F[Option[AuthToken]] = {
    users.find(newUserInfo.username).flatMap {
      case Some(_) => None.pure[F]
      case None =>
        for {
          storedHash   <- Argon2.hashPw[F](newUserInfo.password)
          user         <- User(newUserInfo.username, newUserInfo.email, storedHash).pure[F]
          _            <- users.create(user)
          accessToken  <- authenticator.encodeAccessToken(user)
          refreshToken <- authenticator.encodeRefreshToken(user.username)
        } yield Some(AuthToken(accessToken, refreshToken))
    }
  }

  override def login(username: String, password: String): F[Option[AuthToken]] =
    for {
      maybeUser <- users.find(username)
      // Option[User].filter(User => IO[Boolean]) => IO[Option[User]]
      maybeValidateUser <- maybeUser.filterA(user =>
        Argon2.checkPwBool[F](password, user.storedHash)
      )
      authToken <- maybeValidateUser.traverse(user =>
        for {
          accessToken  <- authenticator.encodeAccessToken(user)
          refreshToken <- authenticator.encodeRefreshToken(user.username)
        } yield AuthToken(accessToken, refreshToken)
      )
    } yield authToken

  def refreshAccessToken(token: String): F[Option[String]] =
    for {
      decoded <- authenticator.decodeRefreshToken(token)
      maybeUser <- decoded match {
        case Some(value) => users.find(value.username)
        case None        => None.pure[F]
      }
      maybeNewAccessToken <- maybeUser.traverse(user =>
        authenticator.encodeAccessToken(user)
      )
    } yield maybeNewAccessToken

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
