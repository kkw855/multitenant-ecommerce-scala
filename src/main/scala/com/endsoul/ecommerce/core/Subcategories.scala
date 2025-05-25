// noinspection SqlNoDataSourceInspection,SqlResolve @ table/"categories"
package com.endsoul.ecommerce.core

import cats.implicits.*
import cats.effect.MonadCancelThrow

import org.typelevel.log4cats.Logger

import doobie.Transactor
import doobie.implicits.*
import doobie.postgres.implicits.* // for java.util.UUID type

import com.endsoul.ecommerce.domain.subcategory.Subcategory

trait Subcategories[F[_]] {
  def all(): F[List[Subcategory]]
}

class LiveSubcategories[F[_]: {MonadCancelThrow, Logger}] private (xa: Transactor[F])
    extends Subcategories[F] {
  override def all(): F[List[Subcategory]] =
    sql"""
      SELECT
        category_id,
        name,
        slug
      FROM subcategories
    """
      .query[Subcategory]
      .to[List]
      .transact(xa)
}

object LiveSubcategories {
  def apply[F[_]: {MonadCancelThrow, Logger}](xa: Transactor[F]): F[LiveSubcategories[F]] =
    new LiveSubcategories[F](xa).pure[F]
}
