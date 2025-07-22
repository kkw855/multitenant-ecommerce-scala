package com.endsoul.ecommerce.core

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.testing.scalatest.AsyncIOSpec

import cats.effect.IO

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import com.endsoul.ecommerce.fixtures.CategoryFixture

class CategoriesSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with DoobieSpec
    with CategoryFixture {

  override val initScript: String = "sql/categories.sql"

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  "Categories 'algebra'" - {
    "should retrieve all categories" in {
      transactor.use { xa =>
        val program = for {
          categories <- LiveCategories[IO](xa)
          retrieved  <- categories.all()
        } yield retrieved

        program.asserting(_ shouldBe List(DesignCategory, SoftwareCategory))
      }
    }
  }
}
