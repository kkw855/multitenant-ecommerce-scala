package com.endsoul.ecommerce.domain

import java.time.Instant

object auth {
  final case class LoginInfo(
      username: String,
      password: String
  )

  final case class NewPasswordInfo(
      oldPassword: String,
      newPassword: String
  )

  final case class AuthToken(
      accessToken: String,
      refreshToken: String
  )

  final case class AccessTokenPayLoad(username: String, email: String, createdAt: Instant, updatedAt: Option[Instant])
  final case class RefreshTokenPayLoad(username: String)

  final case class Token(accessToken: String)
}
