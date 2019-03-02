package com.adrianrafo.gcp4s.vision

import java.net.URI

import cats.syntax.functor._
import cats.effect.{Effect, Resource}
import com.adrianrafo.gcp4s.Gcp4sCredentialsProvider
import com.google.cloud.vision.v1.ImageSource

object Gcp4sVision {

  def createClient[F[_]](maybeCredentialsProvider: Option[Gcp4sCredentialsProvider[F]] = None)(
      implicit E: Effect[F],
      visionAPI: VisionAPI[F]): Resource[F, VisionClient[F]] =
    Resource.make(VisionClient.buildSettings(maybeCredentialsProvider).map(new VisionClient(_)))(
      _.shutdown())

  def createImageSource[F[_]](uri: URI)(implicit E: Effect[F]): F[ImageSource] =
    E.delay(ImageSource.newBuilder().setImageUri(uri.toString).build())

}
