package com.adrianrafo.gcp4s.vision

import cats.MonadError
import cats.syntax.functor._
import cats.syntax.either._
import cats.effect.Sync

class ErrorHandlerService[F[_]](implicit S: Sync[F], ME: MonadError[F, Throwable]) {

  def handleError[A](f: => A): F[Either[VisionError, A]] =
    ME.attempt[A](S.delay(f)).map(_.leftMap(e => VisionError(e.getMessage)))

}
