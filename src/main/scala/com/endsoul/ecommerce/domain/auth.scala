package com.endsoul.ecommerce.domain

object auth {
  final case class LoginInfo(
      username: String,
      password: String
  )

  final case class NewPasswordInfo(
      oldPassword: String,
      newPassword: String
  )

  final case class TokenPayLoad(username: String, email: String)

  final case class Token(accessToken: String)
}
