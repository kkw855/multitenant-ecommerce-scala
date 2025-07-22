package com.endsoul.ecommerce.http.routes

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import cats.effect.IO

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Method, Request, Status}
import org.http4s.implicits.uri

import com.endsoul.ecommerce.core.{Categories, Subcategories}
import com.endsoul.ecommerce.domain.{category, subcategory}
import com.endsoul.ecommerce.domain.category.*
import com.endsoul.ecommerce.fixtures.SubcategoryFixture

class CategoryRouteSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with Http4sDsl[IO]
    with SubcategoryFixture {

  //////////////////////////////////////////////////////////////////////////////////
  // prep
  //////////////////////////////////////////////////////////////////////////////////
  val categories: Categories[IO] = () => IO.pure(List(SoftwareCategory, DesignCategory))
  val subcategories: Subcategories[IO] = () =>
    IO.pure(List(FrontendSubcategory, BackendSubcategory, UserExperienceSubcategory))

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  // this is what we are testing
  val categoryRoutes: HttpRoutes[IO] = CategoryRoutes[IO](categories, subcategories).routes

  //////////////////////////////////////////////////////////////////////////////////
  // tests
  //////////////////////////////////////////////////////////////////////////////////
  "CategoryRoutes" - {
    "should return all categories" in {
      for {
        response <- categoryRoutes.orNotFound.run(
          Request(method = Method.GET, uri = uri"/categories")
        )
        retrieved <- response.as[List[Category]]
      } yield {
        response.status shouldBe Status.Ok
        retrieved shouldBe List(
          Category(SoftwareCategory, List(FrontendSubcategory, BackendSubcategory)),
          Category(DesignCategory, List(UserExperienceSubcategory))
        )
      }
    }
  }
}
