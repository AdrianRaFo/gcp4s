package com.adrianrafo.gcp4s

import cats.Functor
import cats.syntax.functor._
import com.google.api.gax.core.BackgroundResource

abstract class Gcp4sClient[F[_]: Functor, T <: BackgroundResource] {
  private[gcp4s] val client: F[T]

  def shutdown(): F[Unit] = client.map(_.shutdown())
}
