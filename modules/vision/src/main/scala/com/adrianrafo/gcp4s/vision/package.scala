package com.adrianrafo.gcp4s

import cats.effect.Sync
import com.google.cloud.vision.v1._

package object vision {

  import syntax._

  type VisionResponse = Either[VisionError, List[VisionLabel]]

  def visionErrorHandler: Throwable => VisionError = (e: Throwable) => VisionError(e.getMessage)

  implicit def imageAnnotatorClientOps[F[_]: Sync](
      client: ImageAnnotatorClient): ImageAnnotatorClientOps[F] =
    new ImageAnnotatorClientOps[F](client)

  implicit def batchAnnotateImagesResponseOps(
      response: BatchAnnotateImagesResponse): BatchAnnotateImagesResponseOps =
    new BatchAnnotateImagesResponseOps(response)

}
