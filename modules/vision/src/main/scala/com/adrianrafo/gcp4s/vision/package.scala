package com.adrianrafo.gcp4s

import com.google.cloud.vision.v1._

package object vision {

  import syntax._

  type VisionResponse = Either[VisionError, List[VisionLabel]]

  implicit def imageAnnotatorClientOps[F[_]](client: ImageAnnotatorClient)(
      implicit EHS: ErrorHandlerService[F]): ImageAnnotatorClientOps[F] =
    new ImageAnnotatorClientOps[F](client)

  implicit def batchAnnotateImagesResponseOps(
      response: BatchAnnotateImagesResponse): BatchAnnotateImagesResponseOps =
    new BatchAnnotateImagesResponseOps(response)

}
