package com.adrianrafo.gcp4s.vision

import java.net.URI

import cats.effect.Effect
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import com.adrianrafo.gcp4s.vision.RequestBuilder._
import com.google.cloud.vision.v1._

import scala.concurrent.ExecutionContext

trait VisionAPI[F[_]] {
  def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient]

  def createImageSource(uri: URI): F[ImageSource]

  def labelImage(
      client: ImageAnnotatorClient,
      context: Option[ImageContext],
      maxResults: Option[Int],
      fileList: VisionSource*): F[VisionResponse[VisionLabelResponse]]

  /**
   * To detect handwritten text:
   *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
   */
  def textDetection(
      client: ImageAnnotatorClient,
      context: Option[ImageContext],
      fileList: VisionSource*): F[VisionResponse[VisionTextResponse]]

  /**
   * To detect handwritten text:
   *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
   */
  def documentTextDetection(
      client: ImageAnnotatorClient,
      context: Option[ImageContext],
      fileList: VisionSource*): F[VisionResponse[VisionDocument]]

  def faceDetection(
      client: ImageAnnotatorClient,
      context: Option[ImageContext],
      maxResults: Option[Int],
      fileList: VisionSource*): F[VisionResponse[VisionFaceResponse]]

  def logoDetection(
      client: ImageAnnotatorClient,
      context: Option[ImageContext],
      maxResults: Option[Int],
      fileList: VisionSource*): F[VisionResponse[VisionLogoResponse]]

  def cropHints(
      client: ImageAnnotatorClient,
      context: Option[ImageContext],
      fileList: VisionSource*): F[VisionResponse[VisionCropHintResponse]]

  def landmarkDetection(
      client: ImageAnnotatorClient,
      context: Option[ImageContext],
      maxResults: Option[Int],
      fileList: VisionSource*): F[VisionResponse[VisionLandMarkResponse]]

  def imagePropertiesDetection(
      client: ImageAnnotatorClient,
      context: Option[ImageContext],
      fileList: VisionSource*): F[VisionResponse[VisionImageProperties]]

  def safeSearchDetection(
      client: ImageAnnotatorClient,
      context: Option[ImageContext],
      fileList: VisionSource*): F[VisionResponse[VisionSafeSearch]]

  def webEntitiesDetection(
      client: ImageAnnotatorClient,
      context: Option[ImageContext],
      maxResults: Option[Int],
      fileList: VisionSource*): F[VisionResponse[VisionWebDetection]]

  def objectDetection(
      client: ImageAnnotatorClient,
      context: Option[ImageContext],
      maxResults: Option[Int],
      fileList: VisionSource*): F[VisionResponse[VisionObjectResponse]]

}
object VisionAPI {

  def apply[F[_]](implicit E: Effect[F], EC: ExecutionContext): VisionAPI[F] =
    new VisionAPI[F] {
      type VisionApiResult[A] = VisionResult[F, A]

      private def doRequest[T](
          client: ImageAnnotatorClient,
          requestType: Feature.Type,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*)(
          processResult: BatchAnnotateImagesResponse => VisionResponse[T]): F[VisionResponse[T]] = {
        def getBatchRequest(
            fileList: List[VisionSource],
            context: Option[ImageContext],
            feature: Feature.Type,
            maxResults: Option[Int]): VisionApiResult[List[AnnotateImageRequest]] =
          fileList.traverse[VisionApiResult, AnnotateImageRequest](filePath =>
            buildImageRequest(filePath, feature, context, maxResults))

        (for {
          batchRequest <- getBatchRequest(fileList.toList, context, requestType, maxResults)
          response     <- client.sendRequest(toBatchRequest(batchRequest))
        } yield processResult(response)).fold(e => List(e.asLeft[T]), identity)
      }

      def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient] =
        E.catchNonFatal(settings.fold(ImageAnnotatorClient.create())(ImageAnnotatorClient.create))

      def createImageSource(uri: URI): F[ImageSource] =
        E.delay(ImageSource.newBuilder().setImageUri(uri.toString).build())

      def labelImage(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionLabelResponse]] =
        doRequest(client, Feature.Type.LABEL_DETECTION, context, maxResults, fileList: _*)(
          _.processLabels)

      /**
       * To detect handwritten text:
       *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
       */
      def textDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          fileList: VisionSource*): F[VisionResponse[VisionTextResponse]] =
        doRequest(client, Feature.Type.TEXT_DETECTION, context, None, fileList: _*)(_.processText)

      /**
       * To detect handwritten text:
       *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
       */
      def documentTextDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          fileList: VisionSource*): F[VisionResponse[VisionDocument]] =
        doRequest(client, Feature.Type.DOCUMENT_TEXT_DETECTION, context, None, fileList: _*)(
          _.processDocumentText)

      def faceDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionFaceResponse]] =
        doRequest(client, Feature.Type.FACE_DETECTION, context, maxResults, fileList: _*)(
          _.processFace)

      def logoDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionLogoResponse]] =
        doRequest(client, Feature.Type.LOGO_DETECTION, context, maxResults, fileList: _*)(
          _.processLogo)

      def cropHints(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          fileList: VisionSource*): F[VisionResponse[VisionCropHintResponse]] =
        doRequest(client, Feature.Type.CROP_HINTS, context, None, fileList: _*)(_.processCropHints)

      def landmarkDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionLandMarkResponse]] =
        doRequest(client, Feature.Type.LANDMARK_DETECTION, context, maxResults, fileList: _*)(
          _.processLandmark)

      def imagePropertiesDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          fileList: VisionSource*): F[VisionResponse[VisionImageProperties]] =
        doRequest(client, Feature.Type.IMAGE_PROPERTIES, context, None, fileList: _*)(
          _.processImageProperties)

      def safeSearchDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          fileList: VisionSource*): F[VisionResponse[VisionSafeSearch]] =
        doRequest(client, Feature.Type.SAFE_SEARCH_DETECTION, context, None, fileList: _*)(
          _.processSafeSearch)

      def webEntitiesDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionWebDetection]] =
        doRequest(client, Feature.Type.WEB_DETECTION, context, maxResults, fileList: _*)(
          _.processWebEntities)

      def objectDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionObjectResponse]] =
        doRequest(client, Feature.Type.OBJECT_LOCALIZATION, context, maxResults, fileList: _*)(
          _.processObjectDetection)
    }

}
