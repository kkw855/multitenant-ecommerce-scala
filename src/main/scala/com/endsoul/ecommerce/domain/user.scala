package com.endsoul.ecommerce.domain

import java.time.Instant

object user {
  final case class User(
      username: String,
      email: String,
      storedHash: String,
      createdAt: Instant = null,
      updatedAt: Option[Instant] = None
  )

  final case class NewUserInfo(
      username: String,
      email: String,
      password: String
  )
}
