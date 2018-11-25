package com.adrianrafo.gcp4s

import cats.syntax.functor._
import cats.syntax.either._
import cats.effect._

import scala.concurrent.{ExecutionContext, Future}

object ErrorHandlerService {
  def handleError[F[_], E, A](f: => A, handler: Throwable => E)(
      implicit E: Effect[F]): F[Either[E, A]] =
    E.attempt[A](E.delay(f)).map(_.leftMap(handler))

  def asyncHandleError[F[_], E, A](f: => A, handler: Throwable => E)(
      implicit E: Effect[F],
      EC: ExecutionContext): F[Either[E, A]] =
    E.attempt[A](E.async[A](effect => Future(f).onComplete(res => effect(Either.fromTry[A](res)))))
      .map(_.leftMap(handler))

}
