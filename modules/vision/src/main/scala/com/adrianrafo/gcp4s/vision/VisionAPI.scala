package com.adrianrafo.gcp4s.vision

import cats.effect.Effect
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import com.google.cloud.vision.v1._
import com.adrianrafo.gcp4s.vision.RequestBuilder._

import scala.concurrent.ExecutionContext

trait VisionAPI[F[_]] {
  def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient]

  def labelImage(
      client: ImageAnnotatorClient,
      filePath: VisionSource,
      context: Option[ImageContext],
      maxResults: Option[Int]): F[VisionResponse[VisionLabelResponse]]
  def labelImageBatch(
      client: ImageAnnotatorClient,
      fileList: List[VisionSource],
      context: Option[ImageContext],
      maxResults: Option[Int]): F[VisionBatchResponse[VisionLabelResponse]]

  /**
   * To detect handwritten text:
   *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
   */
  def textDetection(
      client: ImageAnnotatorClient,
      filePath: VisionSource,
      context: Option[ImageContext]): F[VisionResponse[VisionTextResponse]]
  def textDetectionBatch(
      client: ImageAnnotatorClient,
      fileList: List[VisionSource],
      context: Option[ImageContext]): F[VisionBatchResponse[VisionTextResponse]]

  /**
   * To detect handwritten text:
   *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
   */
  def documentTextDetection(
      client: ImageAnnotatorClient,
      filePath: VisionSource,
      context: Option[ImageContext]): F[VisionResponse[VisionDocument]]
  def documentTextDetectionBatch(
      client: ImageAnnotatorClient,
      fileList: List[VisionSource],
      context: Option[ImageContext]): F[VisionBatchResponse[VisionDocument]]

  def faceDetection(
      client: ImageAnnotatorClient,
      filePath: VisionSource,
      context: Option[ImageContext]): F[VisionResponse[VisionFaceResponse]]
  def faceDetectionBatch(
      client: ImageAnnotatorClient,
      fileList: List[VisionSource],
      context: Option[ImageContext]): F[VisionBatchResponse[VisionFaceResponse]]

  def logoDetection(
      client: ImageAnnotatorClient,
      filePath: VisionSource,
      context: Option[ImageContext]): F[VisionResponse[VisionLogoResponse]]
  def logoDetectionBatch(
      client: ImageAnnotatorClient,
      fileList: List[VisionSource],
      context: Option[ImageContext]): F[VisionBatchResponse[VisionLogoResponse]]

  def cropHints(
      client: ImageAnnotatorClient,
      filePath: VisionSource,
      context: Option[ImageContext]): F[VisionResponse[VisionCropHintResponse]]
  def cropHintsBatch(
      client: ImageAnnotatorClient,
      fileList: List[VisionSource],
      context: Option[ImageContext]): F[VisionBatchResponse[VisionCropHintResponse]]

  def landmarkDetection(
      client: ImageAnnotatorClient,
      filePath: VisionSource,
      context: Option[ImageContext]): F[VisionResponse[VisionLandMarkResponse]]
  def landmarkDetectionBatch(
      client: ImageAnnotatorClient,
      fileList: List[VisionSource],
      context: Option[ImageContext]): F[VisionBatchResponse[VisionLandMarkResponse]]

  def imagePropertiesDetection(
      client: ImageAnnotatorClient,
      filePath: VisionSource,
      context: Option[ImageContext]): F[VisionResponse[VisionImageProperties]]
  def imagePropertiesDetectionBatch(
      client: ImageAnnotatorClient,
      fileList: List[VisionSource],
      context: Option[ImageContext]): F[VisionBatchResponse[VisionImageProperties]]

  def safeSearchDetection(
      client: ImageAnnotatorClient,
      filePath: VisionSource,
      context: Option[ImageContext]): F[VisionResponse[VisionSafeSearch]]
  def safeSearchDetectionBatch(
      client: ImageAnnotatorClient,
      fileList: List[VisionSource],
      context: Option[ImageContext]): F[VisionBatchResponse[VisionSafeSearch]]

  def webEntitiesDetection(
      client: ImageAnnotatorClient,
      filePath: VisionSource,
      context: Option[ImageContext]): F[VisionResponse[VisionWebDetection]]
  def webEntitiesDetectionBatch(
      client: ImageAnnotatorClient,
      fileList: List[VisionSource],
      context: Option[ImageContext]): F[VisionBatchResponse[VisionWebDetection]]

  def objectDetection(
      client: ImageAnnotatorClient,
      filePath: VisionSource,
      context: Option[ImageContext]): F[VisionResponse[VisionObjectResponse]]
  def objectDetectionBatch(
      client: ImageAnnotatorClient,
      fileList: List[VisionSource],
      context: Option[ImageContext]): F[VisionBatchResponse[VisionObjectResponse]]

}
object VisionAPI {

  def apply[F[_]](implicit E: Effect[F], EC: ExecutionContext): VisionAPI[F] =
    new VisionAPI[F] {
      type VisionApiResult[A] = VisionResult[F, A]

      private def getBatchRequest(
          fileList: List[VisionSource],
          context: Option[ImageContext],
          feature: Feature.Type,
          maxResults: Option[Int]): VisionApiResult[List[AnnotateImageRequest]] =
        fileList.traverse[VisionApiResult, AnnotateImageRequest](filePath =>
          buildImageRequest(filePath, feature, context, maxResults))

      def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient] =
        E.catchNonFatal(settings.fold(ImageAnnotatorClient.create())(ImageAnnotatorClient.create))

      def labelImage(
          client: ImageAnnotatorClient,
          filePath: VisionSource,
          context: Option[ImageContext],
          maxResults: Option[Int]): F[VisionResponse[VisionLabelResponse]] =
        (for {
          request  <- buildImageRequest(filePath, Feature.Type.LABEL_DETECTION, context, maxResults)
          response <- client.sendRequest(toBatchRequest(List(request))).subflatMap(_.processLabels)
        } yield response).value

      def labelImageBatch(
          client: ImageAnnotatorClient,
          fileList: List[Either[String, ImageSource]],
          context: Option[ImageContext],
          maxResults: Option[Int]): F[VisionBatchResponse[VisionLabelResponse]] = {

        (for {
          batchRequest <- getBatchRequest(
            fileList,
            context,
            Feature.Type.LABEL_DETECTION,
            maxResults)
          response <- client.sendRequest(toBatchRequest(batchRequest))
        } yield
          response.processLabelsPerImage).fold(e => List(e.asLeft[VisionLabelResponse]), identity)
      }

      /**
       * To detect handwritten text:
       *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
       */
      def textDetection(
          client: ImageAnnotatorClient,
          filePath: VisionSource,
          context: Option[ImageContext]): F[VisionResponse[VisionTextResponse]] = ???
      def textDetectionBatch(
          client: ImageAnnotatorClient,
          fileList: List[VisionSource],
          context: Option[ImageContext]): F[VisionBatchResponse[VisionTextResponse]] = ???

      /**
       * To detect handwritten text:
       *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
       */
      def documentTextDetection(
          client: ImageAnnotatorClient,
          filePath: VisionSource,
          context: Option[ImageContext]): F[VisionResponse[VisionDocument]] = ???
      def documentTextDetectionBatch(
          client: ImageAnnotatorClient,
          fileList: List[VisionSource],
          context: Option[ImageContext]): F[VisionBatchResponse[VisionDocument]] = ???

      def faceDetection(
          client: ImageAnnotatorClient,
          filePath: VisionSource,
          context: Option[ImageContext]): F[VisionResponse[VisionFaceResponse]] = ???
      def faceDetectionBatch(
          client: ImageAnnotatorClient,
          fileList: List[VisionSource],
          context: Option[ImageContext]): F[VisionBatchResponse[VisionFaceResponse]] = ???

      def logoDetection(
          client: ImageAnnotatorClient,
          filePath: VisionSource,
          context: Option[ImageContext]): F[VisionResponse[VisionLogoResponse]] = ???
      def logoDetectionBatch(
          client: ImageAnnotatorClient,
          fileList: List[VisionSource],
          context: Option[ImageContext]): F[VisionBatchResponse[VisionLogoResponse]] = ???

      def cropHints(
          client: ImageAnnotatorClient,
          filePath: VisionSource,
          context: Option[ImageContext]): F[VisionResponse[VisionCropHintResponse]] = ???
      def cropHintsBatch(
          client: ImageAnnotatorClient,
          fileList: List[VisionSource],
          context: Option[ImageContext]): F[VisionBatchResponse[VisionCropHintResponse]] = ???

      def landmarkDetection(
          client: ImageAnnotatorClient,
          filePath: VisionSource,
          context: Option[ImageContext]): F[VisionResponse[VisionLandMarkResponse]] = ???
      def landmarkDetectionBatch(
          client: ImageAnnotatorClient,
          fileList: List[VisionSource],
          context: Option[ImageContext]): F[VisionBatchResponse[VisionLandMarkResponse]] = ???

      def imagePropertiesDetection(
          client: ImageAnnotatorClient,
          filePath: VisionSource,
          context: Option[ImageContext]): F[VisionResponse[VisionImageProperties]] = ???
      def imagePropertiesDetectionBatch(
          client: ImageAnnotatorClient,
          fileList: List[VisionSource],
          context: Option[ImageContext]): F[VisionBatchResponse[VisionImageProperties]] = ???

      def safeSearchDetection(
          client: ImageAnnotatorClient,
          filePath: VisionSource,
          context: Option[ImageContext]): F[VisionResponse[VisionSafeSearch]] = ???
      def safeSearchDetectionBatch(
          client: ImageAnnotatorClient,
          fileList: List[VisionSource],
          context: Option[ImageContext]): F[VisionBatchResponse[VisionSafeSearch]] = ???

      def webEntitiesDetection(
          client: ImageAnnotatorClient,
          filePath: VisionSource,
          context: Option[ImageContext]): F[VisionResponse[VisionWebDetection]] = ???
      def webEntitiesDetectionBatch(
          client: ImageAnnotatorClient,
          fileList: List[VisionSource],
          context: Option[ImageContext]): F[VisionBatchResponse[VisionWebDetection]] = ???

      def objectDetection(
          client: ImageAnnotatorClient,
          filePath: VisionSource,
          context: Option[ImageContext]): F[VisionResponse[VisionObjectResponse]] = ???
      def objectDetectionBatch(
          client: ImageAnnotatorClient,
          fileList: List[VisionSource],
          context: Option[ImageContext]): F[VisionBatchResponse[VisionObjectResponse]] = ???
    }

}
