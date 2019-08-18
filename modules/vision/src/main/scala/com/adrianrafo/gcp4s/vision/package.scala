package com.adrianrafo.gcp4s

import cats.data.EitherT
import cats.effect.Effect
import com.google.cloud.vision.v1._

import scala.concurrent.ExecutionContext

package object vision {

  import com.adrianrafo.gcp4s.vision.internal.syntax._

  type VisionResult[F[_], A] = EitherT[F, VisionError, A]
  type VisionSource          = Either[String, ImageSource]
  type VisionResponse[A]     = List[Either[VisionError, A]]

  private[vision] def visionErrorHandler: Throwable => VisionError =
    (e: Throwable) => VisionError(e.toString)

  implicit private[vision] def imageAnnotatorClientOps[F[_]: Effect](
      visionClient: VisionClient[F]
  )(implicit EX: ExecutionContext): VisionClientOps[F] =
    new VisionClientOps[F](visionClient)

  implicit private[vision] def batchAnnotateImagesResponseOps(
      response: BatchAnnotateImagesResponse
  ): BatchAnnotateImagesResponseOps =
    new BatchAnnotateImagesResponseOps(response)

  implicit def visionApi[F[_]: Effect](implicit EC: ExecutionContext): VisionAPI[F] = VisionAPI[F]

}
