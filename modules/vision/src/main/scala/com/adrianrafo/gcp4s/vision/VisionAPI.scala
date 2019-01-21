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

      private def getBatchRequest(
          fileList: List[VisionSource],
          context: Option[ImageContext],
          feature: Feature.Type,
          maxResults: Option[Int]): VisionApiResult[List[AnnotateImageRequest]] =
        fileList.traverse[VisionApiResult, AnnotateImageRequest](filePath =>
          buildImageRequest(filePath, feature, context, maxResults))

      def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient] =
        E.catchNonFatal(settings.fold(ImageAnnotatorClient.create())(ImageAnnotatorClient.create))

      def createImageSource(uri: URI): F[ImageSource] =
        E.delay(ImageSource.newBuilder().setImageUri(uri.toString).build())

      def labelImage(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionLabelResponse]] =
        (for {
          batchRequest <- getBatchRequest(
            fileList.toList,
            context,
            Feature.Type.LABEL_DETECTION,
            maxResults)
          response <- client.sendRequest(toBatchRequest(batchRequest))
        } yield response.processLabels).fold(e => List(e.asLeft[VisionLabelResponse]), identity)

      /**
       * To detect handwritten text:
       *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
       */
      def textDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          fileList: VisionSource*): F[VisionResponse[VisionTextResponse]] =
        (for {
          batchRequest <- getBatchRequest(
            fileList.toList,
            context,
            Feature.Type.TEXT_DETECTION,
            None)
          response <- client.sendRequest(toBatchRequest(batchRequest))
        } yield response.processText).fold(e => List(e.asLeft[VisionTextResponse]), identity)

      /**
       * To detect handwritten text:
       *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
       */
      def documentTextDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          fileList: VisionSource*): F[VisionResponse[VisionDocument]] =
        (for {
          batchRequest <- getBatchRequest(
            fileList.toList,
            context,
            Feature.Type.DOCUMENT_TEXT_DETECTION,
            None)
          response <- client.sendRequest(toBatchRequest(batchRequest))
        } yield response.processDocumentText).fold(e => List(e.asLeft[VisionDocument]), identity)

      def faceDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionFaceResponse]] =
        (for {
          batchRequest <- getBatchRequest(
            fileList.toList,
            context,
            Feature.Type.FACE_DETECTION,
            maxResults)
          response <- client.sendRequest(toBatchRequest(batchRequest))
        } yield response.processFace).fold(e => List(e.asLeft[VisionFaceResponse]), identity)

      def logoDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionLogoResponse]] =
        (for {
          batchRequest <- getBatchRequest(
            fileList.toList,
            context,
            Feature.Type.LOGO_DETECTION,
            maxResults)
          response <- client.sendRequest(toBatchRequest(batchRequest))
        } yield response.processLogo).fold(e => List(e.asLeft[VisionLogoResponse]), identity)

      def cropHints(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          fileList: VisionSource*): F[VisionResponse[VisionCropHintResponse]] =
        (for {
          batchRequest <- getBatchRequest(
            fileList.toList,
            context,
            Feature.Type.CROP_HINTS,
            None)
          response <- client.sendRequest(toBatchRequest(batchRequest))
        } yield
          response.processCropHints).fold(e => List(e.asLeft[VisionCropHintResponse]), identity)

      def landmarkDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionLandMarkResponse]] =
        (for {
          batchRequest <- getBatchRequest(
            fileList.toList,
            context,
            Feature.Type.LANDMARK_DETECTION,
            maxResults)
          response <- client.sendRequest(toBatchRequest(batchRequest))
        } yield
          response.processLandmark).fold(e => List(e.asLeft[VisionLandMarkResponse]), identity)

      def imagePropertiesDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          fileList: VisionSource*): F[VisionResponse[VisionImageProperties]] =
        (for {
          batchRequest <- getBatchRequest(
            fileList.toList,
            context,
            Feature.Type.IMAGE_PROPERTIES,
            None)
          response <- client.sendRequest(toBatchRequest(batchRequest))
        } yield response.processImageProperties)
          .fold(e => List(e.asLeft[VisionImageProperties]), identity)

      def safeSearchDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          fileList: VisionSource*): F[VisionResponse[VisionSafeSearch]] =
        (for {
          batchRequest <- getBatchRequest(
            fileList.toList,
            context,
            Feature.Type.SAFE_SEARCH_DETECTION,
            None)
          response <- client.sendRequest(toBatchRequest(batchRequest))
        } yield response.processSafeSearch).fold(e => List(e.asLeft[VisionSafeSearch]), identity)

      def webEntitiesDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionWebDetection]] =
        (for {
          batchRequest <- getBatchRequest(
            fileList.toList,
            context,
            Feature.Type.WEB_DETECTION,
            maxResults)
          response <- client.sendRequest(toBatchRequest(batchRequest))
        } yield response.processWebEntities).fold(e => List(e.asLeft[VisionWebDetection]), identity)

      def objectDetection(
          client: ImageAnnotatorClient,
          context: Option[ImageContext],
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionObjectResponse]] =
        (for {
          batchRequest <- getBatchRequest(
            fileList.toList,
            context,
            Feature.Type.OBJECT_LOCALIZATION,
            maxResults)
          response <- client.sendRequest(toBatchRequest(batchRequest))
        } yield
          response.processObjectDetection).fold(e => List(e.asLeft[VisionObjectResponse]), identity)
    }

}
