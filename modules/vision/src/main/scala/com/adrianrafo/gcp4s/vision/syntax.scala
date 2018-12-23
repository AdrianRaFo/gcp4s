package com.adrianrafo.gcp4s.vision

import cats.data.EitherT
import cats.effect.Effect
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
      ErrorHandlerService.asyncHandleError(
        client.batchAnnotateImagesCallable.futureCall(batchRequest).get(),
        visionErrorHandler)

  }

  final class BatchAnnotateImagesResponseOps(batchImageResponse: BatchAnnotateImagesResponse) {

    import scala.collection.JavaConverters._

    private def handleVisionResponse[A](
        handleResponse: AnnotateImageResponse => A): List[VisionResponse[A]] =
      batchImageResponse.getResponsesList.asScala
        .foldLeft(List.empty[VisionResponse[A]]) {
          case (list, res) => list :+ handleErrors(res, handleResponse)
        }

    def processLabels: VisionResponse[List[VisionLabel]] =
      handleErrors(batchImageResponse.getResponses(0), handleLabelResponse)

    def processLabelsPerImage: VisionBatchResponse[List[VisionLabel]] =
      handleVisionResponse(handleLabelResponse)

    def processText: VisionResponse[List[VisionText]] =
      handleErrors(batchImageResponse.getResponses(0), handleTextResponse)

    def processTextPerImage: VisionBatchResponse[List[VisionText]] =
      handleVisionResponse(handleTextResponse)

    def processObjectDetection =
      handleErrors(batchImageResponse.getResponses(0), handleObjectResponse)
    def processObjectDetectionPerImage =
      handleVisionResponse(handleObjectResponse)

    def processFace =
      handleErrors(batchImageResponse.getResponses(0), handleFaceResponse)
    def processFacePerImage =
      handleVisionResponse(handleFaceResponse)

    def processLogo =
      handleErrors(batchImageResponse.getResponses(0), handleLogoResponse)
    def processLogoPerImage =
      handleVisionResponse(handleLogoResponse)

    def processLandmark =
      handleErrors(batchImageResponse.getResponses(0), handleLandmarkResponse)
    def processLandmarkPerImage =
      handleVisionResponse(handleLandmarkResponse)

    def processSafeSearch =
      handleErrors(batchImageResponse.getResponses(0), handleSafeSearchResponse)
    def processSafeSearchPerImage =
      handleVisionResponse(handleSafeSearchResponse)

    def processWebEntities =
      handleErrors(batchImageResponse.getResponses(0), handleWebEntitiesResponse)
    def processWebEntitiesPerImage =
      handleVisionResponse(handleWebEntitiesResponse)

    def processCropHints =
      handleErrors(batchImageResponse.getResponses(0), handleCropHintsResponse)
    def processCropHintsPerImage =
      handleVisionResponse(handleCropHintsResponse)

    def processDocumentText =
      handleErrors(batchImageResponse.getResponses(0), handleDocumentTextResponse)
    def processDocumentTextPerImage =
      handleVisionResponse(handleDocumentTextResponse)

    def processImageProperties =
      handleErrors(batchImageResponse.getResponses(0), handleImagePropertiesResponse)
    def processImagePropertiesPerImage =
      handleVisionResponse(handleImagePropertiesResponse)

  }

}
