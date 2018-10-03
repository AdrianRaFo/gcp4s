package com.adrianrafo.gcp4s

import cats.syntax.functor._
import cats.syntax.either._
import cats.MonadError
import cats.effect.{Async, Sync}

import scala.concurrent.{ExecutionContext, Future}

object ErrorHandlerService {

  def handleError[F[_], E, A](f: => A, handler: Throwable => E)(
      implicit S: Sync[F],
      ME: MonadError[F, Throwable]): F[Either[E, A]] =
    ME.attempt[A](S.delay(f)).map(_.leftMap(handler))

  def asyncHandleError[F[_], E, A](f: => Future[A], handler: Throwable => E)(
      implicit A: Async[F],
      EC: ExecutionContext,
      ME: MonadError[F, Throwable]): F[Either[E, A]] =
    ME.attempt[A](Async[F].async[A](effect => f.onComplete(res => effect(Either.fromTry[A](res)))))
      .map(_.leftMap(handler))

}
