package com.endsoul.ecommerce.security

import cats.effect.kernel.Sync
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64

object Argon2 {
  private val hashLength = 32

  private val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
    .withVersion(Argon2Parameters.ARGON2_VERSION_13)
    .withIterations(2)
    .withMemoryAsKB(66536)
    .withParallelism(1)

  def hashPw[F[_]: Sync](password: String): F[String] = {
    Sync[F].delay  {
      val random = new SecureRandom()
      val salt = new Array[Byte](16)
      random.nextBytes(salt)

      val generate = new Argon2BytesGenerator()
      generate.init(builder.withSalt(salt).build())

      val result = new Array[Byte](hashLength)
      generate.generateBytes(password.getBytes(StandardCharsets.UTF_8), result, 0, result.length)

      // TODO: $salt=$hash= format 사용
      s"${Base64.getEncoder.encodeToString(salt)},${Base64.getEncoder.encodeToString(result)}"
    }
  }

  def checkPwBool[F[_]: Sync](password: String, storedHash: String): F[Boolean] =
    Sync[F].delay {
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
