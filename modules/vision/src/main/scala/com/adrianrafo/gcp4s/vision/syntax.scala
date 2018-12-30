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
        handleResponse: AnnotateImageResponse => A): VisionBatchResponse[A] =
      batchImageResponse.getResponsesList.asScala
        .foldLeft(List.empty[VisionResponse[A]]) {
          case (list, res) => list :+ handleErrors(res, handleResponse)
        }

    def processLabels: VisionResponse[VisionLabelResponse] =
      handleErrors(batchImageResponse.getResponses(0), handleLabelResponse)

    def processLabelsPerImage: VisionBatchResponse[VisionLabelResponse] =
      handleVisionResponse(handleLabelResponse)

    def processText: VisionResponse[VisionTextResponse] =
      handleErrors(batchImageResponse.getResponses(0), handleTextResponse)

    def processTextPerImage: VisionBatchResponse[VisionTextResponse] =
      handleVisionResponse(handleTextResponse)

    def processObjectDetection: VisionResponse[VisionObjectResponse] =
      handleErrors(batchImageResponse.getResponses(0), handleObjectResponse)
    def processObjectDetectionPerImage: VisionBatchResponse[VisionObjectResponse] =
      handleVisionResponse(handleObjectResponse)

    def processFace: VisionResponse[VisionFaceResponse] =
      handleErrors(batchImageResponse.getResponses(0), handleFaceResponse)
    def processFacePerImage: VisionBatchResponse[VisionFaceResponse] =
      handleVisionResponse(handleFaceResponse)

    def processLogo: VisionResponse[VisionLogoResponse] =
      handleErrors(batchImageResponse.getResponses(0), handleLogoResponse)
    def processLogoPerImage: VisionBatchResponse[VisionLogoResponse] =
      handleVisionResponse(handleLogoResponse)

    def processLandmark: VisionResponse[VisionLandMarkResponse] =
      handleErrors(batchImageResponse.getResponses(0), handleLandmarkResponse)
    def processLandmarkPerImage: VisionBatchResponse[VisionLandMarkResponse] =
      handleVisionResponse(handleLandmarkResponse)

    def processSafeSearch: VisionResponse[VisionSafeSearch] =
      handleErrors(batchImageResponse.getResponses(0), handleSafeSearchResponse)
    def processSafeSearchPerImage: VisionBatchResponse[VisionSafeSearch] =
      handleVisionResponse(handleSafeSearchResponse)

    def processWebEntities: VisionResponse[VisionWebDetection] =
      handleErrors(batchImageResponse.getResponses(0), handleWebEntitiesResponse)
    def processWebEntitiesPerImage: VisionBatchResponse[VisionWebDetection] =
      handleVisionResponse(handleWebEntitiesResponse)

    def processCropHints: VisionResponse[VisionCropHintResponse] =
      handleErrors(batchImageResponse.getResponses(0), handleCropHintResponse)
    def processCropHintsPerImage: VisionBatchResponse[VisionCropHintResponse] =
      handleVisionResponse(handleCropHintResponse)

    def processDocumentText: VisionResponse[VisionDocument] =
      handleErrors(batchImageResponse.getResponses(0), handleDocumentTextResponse)
    def processDocumentTextPerImage: VisionBatchResponse[VisionDocument] =
      handleVisionResponse(handleDocumentTextResponse)

    def processImageProperties: VisionResponse[VisionImageProperties] =
      handleErrors(batchImageResponse.getResponses(0), handleImagePropertiesResponse)
    def processImagePropertiesPerImage: VisionBatchResponse[VisionImageProperties] =
      handleVisionResponse(handleImagePropertiesResponse)

  }

}
