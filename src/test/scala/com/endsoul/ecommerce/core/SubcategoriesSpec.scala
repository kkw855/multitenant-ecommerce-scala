package com.endsoul.ecommerce.core

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.testing.scalatest.AsyncIOSpec

import cats.effect.IO

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import com.endsoul.ecommerce.fixtures.SubcategoryFixture

class SubcategoriesSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with DoobieSpec
    with SubcategoryFixture {

  override val initScript: String = "sql/subcategories.sql"

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  "Subcategories 'algebra'" - {
    "should filter subcategories by categoryId" in {
      transactor.use { xa =>
        val program = for {
          categories <- LiveSubcategories[IO](xa)
          retrieved  <- categories.all()
        } yield retrieved

        program.asserting(_ shouldBe List(FrontendSubcategory, BackendSubcategory, UserExperienceSubcategory))
      }
    }
  }
}
