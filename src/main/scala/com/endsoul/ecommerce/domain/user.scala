package com.endsoul.ecommerce.domain

import java.sql.Timestamp

object user {
  final case class User(
      username: String,
      email: String,
      storedHash: String,
      createdAt: Option[Timestamp],
  )

  final case class NewUserInfo(
      username: String,
      email: String,
      password: String
  )
}
