package com.adrianrafo.gcp4s.vision

import cats.data.EitherT
import cats.effect.Effect
import cats.syntax.either._
import com.adrianrafo.gcp4s.ErrorHandlerService
import com.adrianrafo.gcp4s.vision.ResponseHandler._
import com.google.cloud.vision.v1._

import scala.concurrent.ExecutionContext

private[vision] object syntax {

  final class ImageAnnotatorClientOps[F[_]](client: ImageAnnotatorClient)(
      implicit E: Effect[F],
      EC: ExecutionContext) {

    def sendRequest(batchRequest: BatchAnnotateImagesRequest): EitherT[
      F,
      VisionError,
      BatchAnnotateImagesResponse] =
      ErrorHandlerService.handleError(
        client.batchAnnotateImagesCallable.futureCall(batchRequest).get(),
        visionErrorHandler)

  }

  final class BatchAnnotateImagesResponseOps(batchImageResponse: BatchAnnotateImagesResponse) {

    import scala.collection.JavaConverters._

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

    def processLabels: VisionResponse[VisionLabelResponse] =
      handleVisionResponse(handleLabelResponse)

    def processText: VisionResponse[VisionTextResponse] =
      handleVisionResponse(handleTextResponse)

    def processObjectDetection: VisionResponse[VisionObjectResponse] =
      handleVisionResponse(handleObjectResponse)

    def processFace: VisionResponse[VisionFaceResponse] =
      handleVisionResponse(handleFaceResponse)

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
