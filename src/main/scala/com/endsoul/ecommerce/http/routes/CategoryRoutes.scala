package com.endsoul.ecommerce.http.routes

import cats.implicits.*
import cats.effect.Concurrent

import org.typelevel.log4cats.Logger

import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.server.Router

import com.endsoul.ecommerce.core.Categories
import com.endsoul.ecommerce.core.Subcategories
import com.endsoul.ecommerce.domain.category.Category

class CategoryRoutes[F[_]: {Concurrent, Logger}] private (
    categories: Categories[F],
    subcategories: Subcategories[F]
) extends Http4sDsl[F] {
  private val allCategoriesRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    for {
      categoryInfoList <- categories.all()
      subcategoryList  <- subcategories.all()
      subcategoryMap <- subcategoryList.groupBy(_.categoryId).pure[F]
      _ <- println(subcategoryMap).pure[F]
      categories <- categoryInfoList
        .map(categoryInfo =>
          Category(
            categoryInfo,
            subcategoryMap.getOrElse(categoryInfo.id, List())
          )
        )
        .pure[F]
      resp <- Ok(categories)
    } yield resp
  }

  val routes: HttpRoutes[F] = Router(
    "/categories" -> allCategoriesRoute
  )
}

object CategoryRoutes {
  def apply[F[_]: {Concurrent, Logger}](
      categories: Categories[F],
      subcategories: Subcategories[F]
  ): CategoryRoutes[F] =
    new CategoryRoutes[F](categories, subcategories)
}
