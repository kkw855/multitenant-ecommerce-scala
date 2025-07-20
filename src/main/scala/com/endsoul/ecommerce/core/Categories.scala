// noinspection SqlNoDataSourceInspection,SqlResolve @ table/"categories"
package com.endsoul.ecommerce.core

import cats.implicits.*
import cats.effect.MonadCancelThrow

import org.typelevel.log4cats.Logger

import doobie.Transactor
import doobie.implicits.*
import doobie.postgres.implicits.* // for java.util.UUID type

import com.endsoul.ecommerce.domain.category.CategoryInfo

trait Categories[F[_]] {
  def all(): F[List[CategoryInfo]]
}

class LiveCategories[F[_]: {MonadCancelThrow, Logger}] private (xa: Transactor[F])
    extends Categories[F] {
  override def all(): F[List[CategoryInfo]] =
    sql"""
      SELECT
        id,
        name,
        slug,
        color
      FROM categories
      ORDER BY name
    """
      .query[CategoryInfo]
      .to[List]
      .transact(xa)
}

object LiveCategories {
  def apply[F[_]: {MonadCancelThrow, Logger}](xa: Transactor[F]): F[LiveCategories[F]] =
    new LiveCategories[F](xa).pure[F]
}
