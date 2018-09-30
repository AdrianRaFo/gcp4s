package com.adrianrafo.gcp4s

import cats.syntax.functor._
import cats.syntax.either._
import cats.MonadError
import cats.effect.Sync
import com.adrianrafo.gcp4s.vision.VisionError

object ErrorHandlerService {

  def handleError[F[_], A](
      f: => A)(implicit S: Sync[F], ME: MonadError[F, Throwable]): F[Either[VisionError, A]] =
    ME.attempt[A](S.delay(f)).map(_.leftMap(e => VisionError(e.getMessage)))

}
