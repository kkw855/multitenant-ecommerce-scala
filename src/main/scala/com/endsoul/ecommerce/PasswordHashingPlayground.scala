package com.endsoul.ecommerce

import cats.effect.{IO, IOApp}
import cats.Monad
import cats.implicits.*
import cats.effect.implicits.*
import cats.effect.kernel.Sync

import java.security.SecureRandom
import java.nio.charset.StandardCharsets
import java.util.Base64
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

trait PasswordHashing[F[_]] {
  def hashPw(password: String): F[String]
  def checkPwBool(password: String, hash: String): F[Boolean]
}

class Argon2[F[_]: Sync] extends PasswordHashing[F] {
  private val hashLength = 32

  private val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
    .withVersion(Argon2Parameters.ARGON2_VERSION_13)
    .withIterations(2)
    .withMemoryAsKB(66536)
    .withParallelism(1)

  override def hashPw(password: String): F[String] = {
    Sync[F].delay  {
      println("hashPw")
      val random = new SecureRandom()
      val salt = new Array[Byte](16)
      random.nextBytes(salt)

      val generate = new Argon2BytesGenerator()
      generate.init(builder.withSalt(salt).build())

      val result = new Array[Byte](hashLength)
      generate.generateBytes(password.getBytes(StandardCharsets.UTF_8), result, 0, result.length)

      println(salt.mkString("Array(", ", ", ")"))
      println(result.mkString("Array(", ", ", ")"))

      // TODO: $salt=$hash= format 사용
      s"${Base64.getEncoder.encodeToString(salt)},${Base64.getEncoder.encodeToString(result)}"
    }
  }

  override def checkPwBool(password: String, storedHash: String): F[Boolean] =
    Sync[F].delay {
      println("checkPwBool")
      val splits = storedHash.split(',')
      val decodedSalt = Base64.getDecoder.decode(splits(0))
      val decodedHash = Base64.getDecoder.decode(splits(1))

      val verifier = new Argon2BytesGenerator()
      verifier.init(builder.withSalt(decodedSalt).build())
      val testHash = new Array[Byte](hashLength)
      verifier.generateBytes(password.getBytes(StandardCharsets.UTF_8), testHash, 0, testHash.length)

      testHash.sameElements(decodedHash)
    }
}

object PasswordHashingPlayground extends IOApp.Simple {
  val argon2 = new Argon2[IO]

  val chain = for {
        storedHash <- argon2.hashPw("Baeldung")
        _ <- IO.println(storedHash)
        result1 <- argon2.checkPwBool("Baeldung", storedHash)
        result2 <- argon2.checkPwBool("Baeldung2", storedHash)
        _ <- IO.println(result1)
        _ <- IO.println(result2)
  } yield ()

  override def run: IO[Unit] = for {
//    storedHash <- argon2.hashPw("Baeldung")
//    _ <- IO.println(storedHash)
//    result1 <- argon2.checkPwBool("Baeldung", storedHash)
//    result2 <- argon2.checkPwBool("Baeldung2", storedHash)
//    _ <- IO.println(result1)
//    _ <- IO.println(result2)
    _ <- IO.println("End!!!!!")
  } yield ()
}
