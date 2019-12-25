package com.adrianrafo.gcp4s.vision.internal

import cats.effect.Effect
import cats.syntax.functor._
import com.adrianrafo.gcp4s.ErrorHandlerService
import com.adrianrafo.gcp4s.vision._
import com.adrianrafo.gcp4s.vision.internal.ResponseHandler._
import com.google.cloud.vision.v1._

import scala.concurrent.ExecutionContext

private[vision] object syntax {

  final class VisionClientOps[F[_]](visionClient: VisionClient[F])(
    implicit E: Effect[F],
    EC: ExecutionContext
  ) {

    def sendRequest(
      batchRequest: BatchAnnotateImagesRequest
    ): VisionResult[F, BatchAnnotateImagesResponse] =
      ErrorHandlerService
        .handleError(
          visionClient.client.map(_.batchAnnotateImagesCallable.futureCall(batchRequest).get()),
          visionErrorHandler
        )
        .semiflatMap(identity)

  }

  final class BatchAnnotateImagesResponseOps(batchImageResponse: BatchAnnotateImagesResponse)
      extends BatchAnnotateImagesResponseOpsLike {

    def processLabels: VisionResponse[VisionLabelResponse] =
      handleVisionResponse(batchImageResponse)(handleLabelResponse)

    def processText: VisionResponse[VisionTextResponse] =
      handleVisionResponse(batchImageResponse)(handleTextResponse)

    def processObjectDetection: VisionResponse[VisionObjectResponse] =
      handleVisionResponse(batchImageResponse)(handleObjectResponse)

    def processFace: VisionResponse[VisionFaceResponse] =
      handleVisionResponse(batchImageResponse)(handleFaceResponse)

    def processLogo: VisionResponse[VisionLogoResponse] =
      handleVisionResponse(batchImageResponse)(handleLogoResponse)

    def processLandmark: VisionResponse[VisionLandMarkResponse] =
      handleVisionResponse(batchImageResponse)(handleLandmarkResponse)

    def processSafeSearch: VisionResponse[VisionSafeSearch] =
      handleVisionResponse(batchImageResponse)(handleSafeSearchResponse)

    def processWebEntities: VisionResponse[VisionWebDetection] =
      handleVisionResponse(batchImageResponse)(handleWebEntitiesResponse)

    def processCropHints: VisionResponse[VisionCropHintResponse] =
      handleVisionResponse(batchImageResponse)(handleCropHintResponse)

    def processDocumentText: VisionResponse[VisionDocument] =
      handleVisionResponse(batchImageResponse)(handleDocumentTextResponse)

    def processImageProperties: VisionResponse[VisionImageProperties] =
      handleVisionResponse(batchImageResponse)(handleImagePropertiesResponse)

  }

}
