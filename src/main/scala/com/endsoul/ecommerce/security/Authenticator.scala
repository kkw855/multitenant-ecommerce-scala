package com.endsoul.ecommerce.security

import cats.effect.kernel.Sync
import cats.implicits.catsSyntaxApplicativeId
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import pdi.jwt.algorithms.JwtHmacAlgorithm

import java.time.Instant
import java.time.temporal.ChronoUnit
import com.endsoul.ecommerce.domain.auth.*
import com.endsoul.ecommerce.domain.user.*

import scala.util.{Failure, Success}

trait Authenticator[F[_]] {
  val key: String
  val algorithm: JwtHmacAlgorithm

  def encode(user: User): F[(String, String)]
  def decodeToken(token: String): F[Option[TokenPayLoad]]
  def refresh(token: String): F[Option[String]]
}

class LiveAuthenticator[F[_]: Sync] private extends Authenticator[F] {
  // TODO: 지금은 대칭키를 사용하는데 비대칭키 사용으로 변경
  override val key = "secretKey"
  override val algorithm: JwtHmacAlgorithm = JwtAlgorithm.HS256

  override def encode(user: User): F[(String, String)] = {
    Sync[F].delay {
      val accessClaim = JwtClaim(
        // TODO: 사용자가 부여 받은 권한 추가
        content = TokenPayLoad(user.username, user.email).asJson.noSpaces,
        expiration = Some(Instant.now.plusSeconds(15 * 60).getEpochSecond),
        issuedAt = Some(Instant.now.getEpochSecond)
      )

      val accessToken = JwtCirce.encode(accessClaim, key, algorithm)

      val refreshClaim = JwtClaim(
        content = TokenPayLoad(user.username, user.email).asJson.noSpaces,
        expiration = Some(Instant.now.plus(30, ChronoUnit.DAYS).getEpochSecond),
        issuedAt = Some(Instant.now.getEpochSecond)
      )

      val refreshToken = JwtCirce.encode(refreshClaim, key, algorithm)

      (accessToken, refreshToken)
    }
  }

  override def decodeToken(token: String): F[Option[TokenPayLoad]] =
    Sync[F].delay {
      JwtCirce.decode(token, key, Seq(JwtAlgorithm.HS256)) match {
        case Failure(exception) => None
        case Success(value) => decode[TokenPayLoad](value.content) match {
          case Right(value) => Some(value)
          case Left(value) => None
        }
      }
    }

  override def refresh(token: String): F[Option[String]] = ???
}

object LiveAuthenticator {
  def apply[F[_]: Sync] = new LiveAuthenticator[F]
}
