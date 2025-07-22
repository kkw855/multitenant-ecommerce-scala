package com.endsoul.ecommerce.security

import cats.effect.kernel.Sync
import cats.implicits.*

import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import pdi.jwt.algorithms.JwtHmacAlgorithm

import scala.util.{Failure, Success}

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.endsoul.ecommerce.domain.auth.*
import com.endsoul.ecommerce.domain.user.*

trait Authenticator[F[_]] {
  val key: String
  val algorithm: JwtHmacAlgorithm

  def encodeAccessToken(user: User): F[String]
  def encodeRefreshToken(username: String): F[String]
  def decodeRefreshToken(token: String): F[Option[RefreshTokenPayLoad]]
}

class LiveAuthenticator[F[_]: Sync] private extends Authenticator[F] {
  // TODO: 지금은 대칭키를 사용하는데 비대칭키 사용으로 변경
  override val key                         = "secretKey"
  override val algorithm: JwtHmacAlgorithm = JwtAlgorithm.HS256

  def encodeAccessToken(user: User): F[String] =
    Sync[F].delay {
      val claim = JwtClaim(
        // TODO: 사용자가 부여 받은 권한 추가
        content = AccessTokenPayLoad(user.username, user.email, user.createdAt, user.updatedAt).asJson.noSpaces,
        expiration = Some(Instant.now.plusSeconds(15 * 60).getEpochSecond),
        issuedAt = Some(Instant.now.getEpochSecond)
      )

      JwtCirce.encode(claim, key, algorithm)
    }

  def encodeRefreshToken(username: String): F[String] =
    Sync[F].delay {
      val claim = JwtClaim(
        content = RefreshTokenPayLoad(username).asJson.noSpaces,
        expiration = Some(Instant.now.plus(30, ChronoUnit.DAYS).getEpochSecond),
        issuedAt = Some(Instant.now.getEpochSecond)
      )

      JwtCirce.encode(claim, key, algorithm)
    }

  override def decodeRefreshToken(token: String): F[Option[RefreshTokenPayLoad]] =
    Sync[F].delay {
      for {
        decoded <- JwtCirce.decode(token, key, Seq(JwtAlgorithm.HS256)).toOption
        maybeTokenPayload <- decode[RefreshTokenPayLoad](decoded.content) match {
          case Right(value) => Some(value)
          case Left(_)      => None
        }
      } yield maybeTokenPayload
    }
}

object LiveAuthenticator {
  def apply[F[_]: Sync] = new LiveAuthenticator[F]
}
