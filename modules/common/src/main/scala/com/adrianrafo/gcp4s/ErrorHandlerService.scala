package com.adrianrafo.gcp4s

import cats.syntax.functor._
import cats.syntax.either._
import cats.MonadError
import cats.effect.Sync

object ErrorHandlerService {

  def handleError[F[_], E, A](f: => A, handler: Throwable => E)(
      implicit S: Sync[F],
      ME: MonadError[F, Throwable]): F[Either[E, A]] =
    ME.attempt[A](S.delay(f)).map(_.leftMap(handler))

}
