package com.adrianrafo.gcp4s.vision.internal

import cats.effect.Effect
import cats.syntax.either._
import cats.syntax.functor._
import com.adrianrafo.gcp4s.ErrorHandlerService
import com.adrianrafo.gcp4s.vision._
import com.adrianrafo.gcp4s.vision.internal.ResponseHandler._
import com.google.cloud.vision.v1._

import scala.concurrent.ExecutionContext

private[vision] object syntax {

  final class VisionClientOps[F[_]](visionClient: VisionClient[F])(
      implicit E: Effect[F],
      EC: ExecutionContext) {

    def sendRequest(
        batchRequest: BatchAnnotateImagesRequest): VisionResult[F, BatchAnnotateImagesResponse] =
      ErrorHandlerService
        .handleError(
          visionClient.client.map(_.batchAnnotateImagesCallable.futureCall(batchRequest).get()),
          visionErrorHandler)
        .semiflatMap(identity)

  }

  final class BatchAnnotateImagesResponseOps(batchImageResponse: BatchAnnotateImagesResponse) {

    import scala.collection.JavaConverters._

    def processLabels: VisionResponse[VisionLabelResponse] =
      handleVisionResponse(handleLabelResponse)

    def processText: VisionResponse[VisionTextResponse] =
      handleVisionResponse(handleTextResponse)

    def processObjectDetection: VisionResponse[VisionObjectResponse] =
      handleVisionResponse(handleObjectResponse)

    def processFace: VisionResponse[VisionFaceResponse] =
      handleVisionResponse(handleFaceResponse)

    private def handleVisionResponse[A](
        handleResponse: AnnotateImageResponse => A): VisionResponse[A] = {

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

    def processLogo: VisionResponse[VisionLogoResponse] =
      handleVisionResponse(handleLogoResponse)

    def processLandmark: VisionResponse[VisionLandMarkResponse] =
      handleVisionResponse(handleLandmarkResponse)

    def processSafeSearch: VisionResponse[VisionSafeSearch] =
      handleVisionResponse(handleSafeSearchResponse)

    def processWebEntities: VisionResponse[VisionWebDetection] =
      handleVisionResponse(handleWebEntitiesResponse)

    def processCropHints: VisionResponse[VisionCropHintResponse] =
      handleVisionResponse(handleCropHintResponse)

    def processDocumentText: VisionResponse[VisionDocument] =
      handleVisionResponse(handleDocumentTextResponse)

    def processImageProperties: VisionResponse[VisionImageProperties] =
      handleVisionResponse(handleImagePropertiesResponse)

  }

}
