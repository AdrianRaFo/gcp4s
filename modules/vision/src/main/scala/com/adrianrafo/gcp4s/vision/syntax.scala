package com.adrianrafo.gcp4s.vision

import cats.effect.Sync
import cats.syntax.either._
import com.adrianrafo.gcp4s.ErrorHandlerService
import com.google.cloud.vision.v1._

private[vision] object syntax {

  final class ImageAnnotatorClientOps[F[_]: Sync](client: ImageAnnotatorClient) {

    import scala.collection.JavaConverters._

    //TODO make calls Async
    def sendRequest(
        requests: AnnotateImageRequest): F[Either[VisionError, BatchAnnotateImagesResponse]] =
      ErrorHandlerService.handleError(
        client.batchAnnotateImages(List(requests).asJava),
        visionErrorHandler)

    def sendRequestBatch(
        requests: List[AnnotateImageRequest]): F[Either[VisionError, BatchAnnotateImagesResponse]] =
      ErrorHandlerService.handleError(
        client.batchAnnotateImages(requests.asJava),
        visionErrorHandler)

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
            .map(tag => VisionLabel(tag.getDescription, getPercentScore(tag.getScore)))
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
