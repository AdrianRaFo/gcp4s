package com.adrianrafo.gcp4s.vision

import cats.syntax.either._
import com.google.cloud.vision.v1._

private[vision] object syntax {

  final class ImageAnnotatorClientOps[F[_]](client: ImageAnnotatorClient)(
      implicit EHS: ErrorHandlerService[F]) {

    import scala.collection.JavaConverters._

    def labelImage(
        requests: AnnotateImageRequest): F[Either[VisionError, BatchAnnotateImagesResponse]] =
      EHS.handleError(client.batchAnnotateImages(List(requests).asJava))

    def labelImageBatch(
        requests: List[AnnotateImageRequest]): F[Either[VisionError, BatchAnnotateImagesResponse]] =
      EHS.handleError(client.batchAnnotateImages(requests.asJava))

  }

  final class BatchAnnotateImagesResponseOps(response: BatchAnnotateImagesResponse) {

    import scala.collection.JavaConverters._

    private def getPercentScore(score: Float): Int = (score * 100).toInt

    private def handleImageResponse(response: AnnotateImageResponse): VisionResponse =
      response match {
        case res if res.hasError =>
          VisionError(s"Error: ${res.getError}").asLeft[List[VisionLabel]]
        case res if !res.hasError =>
          res.getLabelAnnotationsList.asScala.toList
            .map(tag => VisionLabel(tag.getLocale, getPercentScore(tag.getScore)))
            .asRight[VisionError]
      }

    def getLabelsPerImage: List[VisionResponse] = {
      response.getResponsesList.asScala
        .foldRight(List.empty[Either[VisionError, List[VisionLabel]]]) {
          case (res, list) => list :+ handleImageResponse(res)
        }
    }

    def getLabels: VisionResponse =
      handleImageResponse(response.getResponses(0))

  }

}
