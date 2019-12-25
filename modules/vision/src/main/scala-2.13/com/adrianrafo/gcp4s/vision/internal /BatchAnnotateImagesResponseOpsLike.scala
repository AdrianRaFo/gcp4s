package com.adrianrafo.gcp4s.vision.internal

import cats.syntax.either._
import com.adrianrafo.gcp4s.vision.{VisionError, VisionResponse}
import com.google.cloud.vision.v1.{AnnotateImageResponse, BatchAnnotateImagesResponse}

private[internal] trait BatchAnnotateImagesResponseOpsLike {
  def handleVisionResponse[A](batchImageResponse: BatchAnnotateImagesResponse)(
    handleResponse: AnnotateImageResponse => A
  ): VisionResponse[A] = {
    import scala.jdk.CollectionConverters._

    def handleErrors(response: AnnotateImageResponse): Either[VisionError, A] =
      response match {
        case res if res.hasError => VisionError(s"Error: ${res.getError}").asLeft[A]
        case res                 => handleResponse(res).asRight[VisionError]
      }

    batchImageResponse.getResponsesList.asScala
      .foldLeft(List.empty[Either[VisionError, A]]) {
        case (list, res) => list :+ handleErrors(res)
      }
  }
}
