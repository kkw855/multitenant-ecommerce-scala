package com.endsoul.ecommerce.core

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.Inside
import org.scalatest.OptionValues.*

import cats.effect.*

import doobie.implicits.*
import doobie.postgres._
import doobie.postgres.implicits._
import org.postgresql.util.PSQLException

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import com.endsoul.ecommerce.domain.user.*
import com.endsoul.ecommerce.fixtures.UserFixture

//noinspection SqlNoDataSourceInspection
class UsersSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with DoobieSpec
    with Inside
    with UserFixture {

  override val initScript: String = "sql/users.sql"
  given logger: Logger[IO]        = Slf4jLogger.getLogger[IO]

  "Users 'algebra'" - {
    "should create a new user" in {
      transactor.use { xa =>
        val program = for {
          users    <- LiveUsers[IO](xa)
          username <- users.create(NewUser)
          maybeUser <- sql"SELECT * FROM users WHERE username = ${NewUser.username}"
            .query[User]
            .option
            .transact(xa)
        } yield (username, maybeUser)

        program.asserting { case (username, Some(user)) =>
          username shouldBe NewUser.username
          user.username shouldBe NewUser.username
          user.email shouldBe NewUser.email
          user.storedHash shouldBe NewUser.storedHash
          user.createdAt should not be null
          user.updatedAt shouldBe None
        }
      }
    }

    "should fail creating a new user if the username already exists" in {
      transactor.use { xa =>
        val program = for {
          users  <- LiveUsers[IO](xa)
          userId <- users.create(Lee).attempt // IO[Either[Throwable, String]]
        } yield userId

        program.asserting { outcome =>
          inside(outcome) {
            case Left(e) => e shouldBe a[PSQLException]
            case _       => fail()
          }
        }
      }
    }

    "should retrieve a user by username" in {
      transactor.use { xa =>
        val program = for {
          users     <- LiveUsers[IO](xa)
          retrieved <- users.find(leeUsername)
        } yield retrieved

        program.asserting(_ shouldBe Some(Lee))
      }
    }

    "should retrieve None if the username doesn't exist" in {
      transactor.use { xa =>
        val program = for {
          users     <- LiveUsers[IO](xa)
          retrieved <- users.find("notfoundUserName")
        } yield retrieved

        program.asserting(_ shouldBe None)
      }
    }

    "should return None when updating a user that does not exist" in {
      transactor.use { xa =>
        val program = for {
          users     <- LiveUsers[IO](xa)
          maybeUser <- users.update(NewUser)
        } yield maybeUser

        program.asserting(_ shouldBe None)
      }
    }

    "should update an existing user" in {
      transactor.use { xa =>
        val program = for {
          users     <- LiveUsers[IO](xa)
          maybeUser <- users.update(UpdatedHong)
        } yield maybeUser

        program.asserting {
          inside(_) {
            case Some(
                  User(
                    UpdatedHong.username,
                    UpdatedHong.email,
                    UpdatedHong.storedHash,
                    createdAt,
                    updatedAt
                  )
                ) =>
              createdAt should not be null
              updatedAt shouldBe defined
          }
        }
      }
    }

    "should delete a user" in {
      transactor.use { xa =>
        val program = for {
          users  <- LiveUsers[IO](xa)
          result <- users.delete(leeUsername)
          maybeUser <- sql"SELECT * FROM users WHERE username = $leeUsername"
            .query[User]
            .option
            .transact(xa)
        } yield (result, maybeUser)

        program.asserting(_ should matchPattern { case (true, None) => })
      }
    }
  }
}
