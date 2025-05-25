package com.endsoul.ecommerce.config

import cats.implicits.*
import cats.MonadThrow

import pureconfig.error.ConfigReaderException
import pureconfig.{ConfigReader, ConfigSource}

import scala.reflect.ClassTag

object syntax {
  extension (source: ConfigSource)
    def loadF[F[_], A](using
        reader: ConfigReader[A],
        F: MonadThrow[F],
        classTag: ClassTag[A]
    ): F[A] = {
      F.pure(source.load[A]).flatMap {
        // implicit ct: ClassTag[A]
        case Left(failures) => F.raiseError[A](ConfigReaderException(failures))
        case Right(value)   => F.pure(value)
      }
    }
}
