package com.adrianrafo.gcp4s.vision

import cats.effect.Effect
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import com.google.cloud.vision.v1._
import com.adrianrafo.gcp4s.vision.RequestBuilder._

import scala.concurrent.ExecutionContext

trait VisionImageAPI[F[_]] {
  def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient]

  def labelImage(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext],
      maxResults: Option[Int]): F[VisionResponse[List[VisionLabel]]]

  def labelImageBatch(
      client: ImageAnnotatorClient,
      fileList: List[Either[String, ImageSource]],
      context: Option[ImageContext],
      maxResults: Option[Int]): F[VisionBatchResponse[List[VisionLabel]]]

  /**
   * To detect handwritten text:
   *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
   */
  def textDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionResponse[VisionText]]

  /**
   * To detect handwritten text:
   *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
   */
  def documentTextDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionResponse[VisionDocument]]

  def faceDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionResponse[List[VisionFace]]]

  def logoDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionResponse[List[VisionLogo]]]

  def cropHints(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionResponse[List[VisionCropHints]]]

  def landmarkDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionResponse[List[VisionLandMark]]]

  def imagePropertiesDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionResponse[List[VisionImageProperties]]]

  def safeSearchDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionResponse[VisionSafeSearch]]

  def webEntitiesDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionResponse[List[VisionWebEntity]]]

  def objectDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionResponse[List[VisionObject]]]

}
object VisionImageAPI {

  def apply[F[_]](implicit E: Effect[F], EC: ExecutionContext): VisionImageAPI[F] =
    new VisionImageAPI[F] {
      type VisionApiResult[A] = VisionResult[F, A]

      def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient] =
        E.catchNonFatal(settings.fold(ImageAnnotatorClient.create())(ImageAnnotatorClient.create))

      def labelImage(
          client: ImageAnnotatorClient,
          filePath: Either[String, ImageSource],
          context: Option[ImageContext],
          maxResults: Option[Int]): F[VisionResponse[List[VisionLabel]]] =
        (for {
          request  <- buildImageRequest(filePath, Feature.Type.LABEL_DETECTION, context, maxResults)
          response <- client.sendRequest(toBatchRequest(List(request))).subflatMap(_.processLabels)
        } yield response).value

      def labelImageBatch(
          client: ImageAnnotatorClient,
          fileList: List[Either[String, ImageSource]],
          context: Option[ImageContext],
          maxResults: Option[Int]): F[VisionBatchResponse[List[VisionLabel]]] = {

        def getBatchRequest: VisionApiResult[List[AnnotateImageRequest]] =
          fileList.traverse[VisionApiResult, AnnotateImageRequest](filePath =>
            buildImageRequest(filePath, Feature.Type.LABEL_DETECTION, context, maxResults))

        (for {
          batchRequest <- getBatchRequest
          response     <- client.sendRequest(toBatchRequest(batchRequest))
        } yield
          response.processLabelsPerImage).fold(e => List(e.asLeft[List[VisionLabel]]), identity)
      }

      override def textDetection(
          client: ImageAnnotatorClient,
          filePath: Either[String, ImageSource],
          context: Option[ImageContext]) = ???

      override def documentTextDetection(
          client: ImageAnnotatorClient,
          filePath: Either[String, ImageSource],
          context: Option[ImageContext]) = ???

      override def faceDetection(
          client: ImageAnnotatorClient,
          filePath: Either[String, ImageSource],
          context: Option[ImageContext]) = ???

      override def logoDetection(
          client: ImageAnnotatorClient,
          filePath: Either[String, ImageSource],
          context: Option[ImageContext]) = ???

      override def cropHints(
          client: ImageAnnotatorClient,
          filePath: Either[String, ImageSource],
          context: Option[ImageContext]) = ???

      override def landmarkDetection(
          client: ImageAnnotatorClient,
          filePath: Either[String, ImageSource],
          context: Option[ImageContext]) = ???

      override def imagePropertiesDetection(
          client: ImageAnnotatorClient,
          filePath: Either[String, ImageSource],
          context: Option[ImageContext]) = ???

      override def safeSearchDetection(
          client: ImageAnnotatorClient,
          filePath: Either[String, ImageSource],
          context: Option[ImageContext]) = ???

      override def webEntitiesDetection(
          client: ImageAnnotatorClient,
          filePath: Either[String, ImageSource],
          context: Option[ImageContext]) = ???

      override def objectDetection(
          client: ImageAnnotatorClient,
          filePath: Either[String, ImageSource],
          context: Option[ImageContext]) = ???

    }
}
