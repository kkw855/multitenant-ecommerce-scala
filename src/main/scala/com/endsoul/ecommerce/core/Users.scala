// noinspection SqlNoDataSourceInspection,SqlResolve @ table/"users"
package com.endsoul.ecommerce.core

import cats.*
import cats.implicits.*
import cats.effect.*

import doobie.implicits.*
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.Transactor

import org.typelevel.log4cats.Logger

import com.endsoul.ecommerce.domain.user.User

trait Users[F[_]] {
  def create(user: User): F[String]
  def find(username: String): F[Option[User]]
  def update(user: User): F[Option[User]]
  def delete(username: String): F[Boolean]
}

final class LiveUsers[F[_]: {MonadCancelThrow, Logger}] private (xa: Transactor[F])
    extends Users[F] {
  override def create(user: User): F[String] =
    sql"""
        INSERT INTO users (
          username,
          email,
          stored_hash
        ) VALUES (
          ${user.username},
          ${user.email},
          ${user.storedHash}
        )
      """.update.run
      .transact(xa)
      .map(_ => user.username)

  // TODO: created_at updated_at
  override def find(username: String): F[Option[User]] =
    sql"SELECT * FROM users WHERE username = $username"
      .query[User]
      .option
      .transact(xa)

  override def update(user: User): F[Option[User]] =
    for {
      _ <- sql"""
            UPDATE users SET
              email = ${user.email},
              stored_hash = ${user.storedHash}
            WHERE username = ${user.username}
          """.update.run.transact(xa)
      maybeUser <- find(user.username)
    } yield maybeUser

  override def delete(username: String): F[Boolean] =
    sql"DELETE FROM users WHERE username = $username".update.run.transact(xa).map(_ > 0)
}

object LiveUsers {
  def apply[F[_]: {MonadCancelThrow, Logger}](xa: Transactor[F]): F[LiveUsers[F]] =
    new LiveUsers[F](xa).pure[F]
}
