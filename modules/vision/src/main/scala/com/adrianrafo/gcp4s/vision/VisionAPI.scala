package com.adrianrafo.gcp4s.vision

import java.net.URI

import cats.effect.Effect
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import com.adrianrafo.gcp4s.vision.RequestBuilder._
import com.google.cloud.vision.v1._

import scala.concurrent.ExecutionContext

trait VisionAPI[F[_]] {
  def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient]

  def createImageSource(uri: URI): F[ImageSource]

  def labelImage(
      client: ImageAnnotatorClient,
      maxResults: Option[Int],
      fileList: VisionSource*): F[VisionResponse[VisionLabelResponse]]

  /**
   * To detect handwritten text add "en-t-i0-handwrit" language hint
   * None for autodetect language
   */
  def textDetection(
      client: ImageAnnotatorClient,
      languages: Option[List[String]],
      fileList: VisionSource*): F[VisionResponse[VisionTextResponse]]

  /**
   * To detect handwritten text add "en-t-i0-handwrit" language hint
   * None for autodetect language
   */
  def documentTextDetection(
      client: ImageAnnotatorClient,
      languages: Option[List[String]],
      fileList: VisionSource*): F[VisionResponse[VisionDocument]]

  def faceDetection(
      client: ImageAnnotatorClient,
      maxResults: Option[Int],
      fileList: VisionSource*): F[VisionResponse[VisionFaceResponse]]

  def logoDetection(
      client: ImageAnnotatorClient,
      maxResults: Option[Int],
      fileList: VisionSource*): F[VisionResponse[VisionLogoResponse]]

  /**
   * Aspect ratios in floats, representing the ratio of the width to the height
   *        of the image. For example, if the desired aspect ratio is 4/3, the
   *        corresponding float value should be 1.33333.  If not specified, the
   *        best possible crop is returned. The number of provided aspect ratios is
   *        limited to a maximum of 16; any aspect ratios provided after the 16th are
   *        ignored.
   *
   */
  def cropHints(
      client: ImageAnnotatorClient,
      aspectRatios: Option[List[Float]],
      fileList: VisionSource*): F[VisionResponse[VisionCropHintResponse]]

  def landmarkDetection(
      client: ImageAnnotatorClient,
      maxResults: Option[Int],
      fileList: VisionSource*): F[VisionResponse[VisionLandMarkResponse]]

  def imagePropertiesDetection(
      client: ImageAnnotatorClient,
      fileList: VisionSource*): F[VisionResponse[VisionImageProperties]]

  def safeSearchDetection(
      client: ImageAnnotatorClient,
      fileList: VisionSource*): F[VisionResponse[VisionSafeSearch]]

  def webEntitiesDetection(
      client: ImageAnnotatorClient,
      includeGeoLocation: Boolean,
      maxResults: Option[Int],
      fileList: VisionSource*): F[VisionResponse[VisionWebDetection]]

  def objectDetection(
      client: ImageAnnotatorClient,
      maxResults: Option[Int],
      fileList: VisionSource*): F[VisionResponse[VisionObjectResponse]]

}
object VisionAPI {

  def apply[F[_]](implicit E: Effect[F], EC: ExecutionContext): VisionAPI[F] =
    new VisionAPI[F] {

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
            maxResults: Option[Int]): F[VisionResponse[AnnotateImageRequest]] =
          fileList.traverse(filePath =>
            buildImageRequest(filePath, feature, context, maxResults).value)

        def separateResults(list: VisionResponse[AnnotateImageRequest]): (
            List[VisionError],
            List[AnnotateImageRequest]) =
          list.foldLeft((List.empty[VisionError], List.empty[AnnotateImageRequest])) {
            case ((errAcc, reqAcc), Right(req)) => (errAcc, reqAcc :+ req)
            case ((errAcc, reqAcc), Left(err))  => (errAcc :+ err, reqAcc)
          }

        getBatchRequest(fileList.toList, context, requestType, maxResults).flatMap(requestList => {
          val (badRequestList, rightRequestList) = separateResults(requestList)
          val errorList: VisionResponse[T]       = badRequestList.map(_.asLeft[T])

          client
            .sendRequest(toBatchRequest(rightRequestList))
            .fold(errorList :+ _.asLeft[T], response => errorList ++ processResult(response))
        })

      }

      def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient] =
        E.catchNonFatal(settings.fold(ImageAnnotatorClient.create())(ImageAnnotatorClient.create))

      def createImageSource(uri: URI): F[ImageSource] =
        E.delay(ImageSource.newBuilder().setImageUri(uri.toString).build())

      def labelImage(
          client: ImageAnnotatorClient,
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionLabelResponse]] =
        doRequest(client, Feature.Type.LABEL_DETECTION, None, maxResults, fileList: _*)(
          _.processLabels)

      /**
       * To detect handwritten text:
       *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
       */
      def textDetection(
          client: ImageAnnotatorClient,
          languages: Option[List[String]],
          fileList: VisionSource*): F[VisionResponse[VisionTextResponse]] =
        for {
          context <- createTextDetectionContext(languages)
          result <- doRequest(client, Feature.Type.TEXT_DETECTION, context, None, fileList: _*)(
            _.processText)
        } yield result

      /**
       * To detect handwritten text:
       *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
       */
      def documentTextDetection(
          client: ImageAnnotatorClient,
          languages: Option[List[String]],
          fileList: VisionSource*): F[VisionResponse[VisionDocument]] =
        for {
          context <- createTextDetectionContext(languages)
          result <- doRequest(
            client,
            Feature.Type.DOCUMENT_TEXT_DETECTION,
            context,
            None,
            fileList: _*)(_.processDocumentText)
        } yield result

      def faceDetection(
          client: ImageAnnotatorClient,
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionFaceResponse]] =
        doRequest(client, Feature.Type.FACE_DETECTION, None, maxResults, fileList: _*)(
          _.processFace)

      def logoDetection(
          client: ImageAnnotatorClient,
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionLogoResponse]] =
        doRequest(client, Feature.Type.LOGO_DETECTION, None, maxResults, fileList: _*)(
          _.processLogo)

      def cropHints(
          client: ImageAnnotatorClient,
          aspectRatios: Option[List[Float]],
          fileList: VisionSource*): F[VisionResponse[VisionCropHintResponse]] =
        for {
          context <- createCropHintContext(aspectRatios)
          result <- doRequest(client, Feature.Type.CROP_HINTS, context, None, fileList: _*)(
            _.processCropHints)
        } yield result

      def landmarkDetection(
          client: ImageAnnotatorClient,
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionLandMarkResponse]] =
        doRequest(client, Feature.Type.LANDMARK_DETECTION, None, maxResults, fileList: _*)(
          _.processLandmark)

      def imagePropertiesDetection(
          client: ImageAnnotatorClient,
          fileList: VisionSource*): F[VisionResponse[VisionImageProperties]] =
        doRequest(client, Feature.Type.IMAGE_PROPERTIES, None, None, fileList: _*)(
          _.processImageProperties)

      def safeSearchDetection(
          client: ImageAnnotatorClient,
          fileList: VisionSource*): F[VisionResponse[VisionSafeSearch]] =
        doRequest(client, Feature.Type.SAFE_SEARCH_DETECTION, None, None, fileList: _*)(
          _.processSafeSearch)

      def webEntitiesDetection(
          client: ImageAnnotatorClient,
          includeGeoLocation: Boolean,
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionWebDetection]] =
        for {
          context <- createWebDetectionContext(includeGeoLocation)
          result <- doRequest(
            client,
            Feature.Type.WEB_DETECTION,
            context,
            maxResults,
            fileList: _*)(_.processWebEntities)
        } yield result

      def objectDetection(
          client: ImageAnnotatorClient,
          maxResults: Option[Int],
          fileList: VisionSource*): F[VisionResponse[VisionObjectResponse]] =
        doRequest(client, Feature.Type.OBJECT_LOCALIZATION, None, maxResults, fileList: _*)(
          _.processObjectDetection)
    }

}
