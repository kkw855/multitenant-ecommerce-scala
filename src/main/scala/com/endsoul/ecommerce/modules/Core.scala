package com.endsoul.ecommerce.modules

import cats.effect.{Async, Resource}
import org.typelevel.log4cats.Logger
import doobie.Transactor
import com.endsoul.ecommerce.core.{Auth, Categories, LiveAuth, LiveCategories, LiveSubcategories, LiveUsers, Subcategories, Users}
import com.endsoul.ecommerce.security.LiveAuthenticator

final class Core[F[_]] private(val users: Users[F], val categories: Categories[F], val subcategories: Subcategories[F], val auth: Auth[F])

object Core {
  def apply[F[_]: {Async, Logger}](xa: Transactor[F]): Resource[F, Core[F]] = for {
    users <- Resource.eval(LiveUsers[F](xa))
    categories <- Resource.eval(LiveCategories[F](xa))
    subcategories <- Resource.eval(LiveSubcategories[F](xa))
    auth <- Resource.eval(LiveAuth[F](users, LiveAuthenticator[F]))
  } yield new Core[F](users, categories, subcategories, auth)
}
