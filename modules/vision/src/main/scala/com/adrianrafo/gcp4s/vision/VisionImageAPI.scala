package com.adrianrafo.gcp4s.vision

import cats.effect.Effect
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import com.google.cloud.vision.v1._
import com.adrianrafo.gcp4s.vision.utils._

import scala.concurrent.ExecutionContext

trait VisionImageAPI[F[_]] {
  def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient]

  def labelImage(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext],
      maxResults: Option[Int]): F[VisionLabelResponse]

  def labelImageBatch(
      client: ImageAnnotatorClient,
      fileList: List[Either[String, ImageSource]],
      context: Option[ImageContext],
      maxResults: Option[Int]): F[List[VisionLabelResponse]]

  /**
   * To detect handwritten text:
   *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
   */
  def textDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionLabelResponse]

  /**
   * To detect handwritten text:
   *  ImageContext imageContext = ImageContext.newBuilder().addLanguageHints("en-t-i0-handwrit").build();
   */
  def documentTextDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionLabelResponse]

  def faceDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionLabelResponse]

  def logoDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionLabelResponse]

  def cropHints(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionLabelResponse]

  def landmarkDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionLabelResponse]

  def imagePropertiesDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionLabelResponse]

  def safeSearchDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionLabelResponse]

  def webEntitiesDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionLabelResponse]

  def objectDetection(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionLabelResponse]

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
          maxResults: Option[Int]): F[VisionLabelResponse] =
        (for {
          request  <- buildImageRequest(filePath, Feature.Type.LABEL_DETECTION, context, maxResults)
          response <- client.sendRequest(toBatchRequest(List(request))).subflatMap(_.processLabels)
        } yield response).value

      def labelImageBatch(
          client: ImageAnnotatorClient,
          fileList: List[Either[String, ImageSource]],
          context: Option[ImageContext],
          maxResults: Option[Int]): F[List[VisionLabelResponse]] = {

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
          filePath: scala.Either[String, ImageSource],
          context: Option[ImageContext]): F[VisionLabelResponse] = ???

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
