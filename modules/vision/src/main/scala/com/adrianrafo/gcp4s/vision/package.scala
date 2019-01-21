package com.adrianrafo.gcp4s

import cats.data.EitherT
import cats.effect.Effect
import com.google.cloud.vision.v1._

import scala.concurrent.ExecutionContext

package object vision {

  import syntax._

  type VisionResult[F[_], A] = EitherT[F, VisionError, A]
  type VisionSource          = Either[String, ImageSource]
  type VisionResponse[A]     = List[Either[VisionError, A]]

  def visionErrorHandler: Throwable => VisionError = (e: Throwable) => VisionError(e.getMessage)

  implicit def imageAnnotatorClientOps[F[_]: Effect](client: ImageAnnotatorClient)(
      implicit EX: ExecutionContext): ImageAnnotatorClientOps[F] =
    new ImageAnnotatorClientOps[F](client)

  implicit def batchAnnotateImagesResponseOps(
      response: BatchAnnotateImagesResponse): BatchAnnotateImagesResponseOps =
    new BatchAnnotateImagesResponseOps(response)

}
