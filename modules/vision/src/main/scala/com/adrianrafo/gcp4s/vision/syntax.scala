package com.adrianrafo.gcp4s.vision

import cats.data.EitherT
import cats.effect.Effect
import cats.syntax.either._
import com.adrianrafo.gcp4s.ErrorHandlerService
import com.google.cloud.vision.v1._

import scala.concurrent.ExecutionContext

private[vision] object syntax {

  final class ImageAnnotatorClientOps[F[_]](client: ImageAnnotatorClient)(
      implicit E: Effect[F],
      EC: ExecutionContext) {

    import scala.collection.JavaConverters._

    private def toBatchRequest(requests: List[AnnotateImageRequest]): BatchAnnotateImagesRequest =
      BatchAnnotateImagesRequest.newBuilder().addAllRequests(requests.asJava).build()

    def sendRequest(
        requests: AnnotateImageRequest): EitherT[F, VisionError, BatchAnnotateImagesResponse] =
      ErrorHandlerService.asyncHandleError(
        client.batchAnnotateImagesCallable.futureCall(toBatchRequest(List(requests))).get(),
        visionErrorHandler)

    def sendRequestBatch(requests: List[AnnotateImageRequest]): EitherT[
      F,
      VisionError,
      BatchAnnotateImagesResponse] =
      ErrorHandlerService.asyncHandleError(
        client.batchAnnotateImagesCallable.futureCall(toBatchRequest(requests)).get(),
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
