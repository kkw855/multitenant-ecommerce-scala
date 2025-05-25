package com.endsoul.ecommerce.modules

import cats.effect.{Async, Resource}

import org.typelevel.log4cats.Logger

import doobie.Transactor

import com.endsoul.ecommerce.core.{Categories, LiveCategories, Subcategories, LiveSubcategories}

final class Core[F[_]] private(val categories: Categories[F], val subcategories: Subcategories[F])

object Core {
  def apply[F[_]: {Async, Logger}](xa: Transactor[F]): Resource[F, Core[F]] = for {
    categories <- Resource.eval(LiveCategories[F](xa))
    subcategories <- Resource.eval(LiveSubcategories[F](xa))
  } yield new Core[F](categories, subcategories)
}
