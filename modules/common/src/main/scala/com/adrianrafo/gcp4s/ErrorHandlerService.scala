package com.adrianrafo.gcp4s

import cats.data.EitherT
import cats.effect._
import cats.syntax.either._

import scala.concurrent.{ExecutionContext, Future}

object ErrorHandlerService {

  def handleError[F[_], E, A](f: => A, handler: Throwable => E)(
      implicit E: Effect[F],
      EC: ExecutionContext): EitherT[F, E, A] =
    E.attemptT[A](E.async[A](effect =>
        Future(f).onComplete(res => effect(Either.fromTry[A](res)))))
      .leftMap(handler)

}
