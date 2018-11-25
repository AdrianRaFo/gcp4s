package com.adrianrafo.gcp4s.vision

import java.nio.file._

import cats.MonadError
import cats.data.EitherT
import cats.effect.Effect
import cats.instances.list._
import cats.syntax.either._
import cats.syntax.traverse._
import com.adrianrafo.gcp4s.ErrorHandlerService
import com.google.cloud.vision.v1.Feature.Type
import com.google.cloud.vision.v1._
import com.google.protobuf.ByteString

import scala.concurrent.ExecutionContext

trait VisionAPI[F[_]] {
  def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient]

  def labelImage(
      client: ImageAnnotatorClient,
      filePath: Either[String, ImageSource],
      context: Option[ImageContext]): F[VisionResponse]

  def labelImageBatch(
      client: ImageAnnotatorClient,
      fileList: List[Either[String, ImageSource]],
      context: Option[ImageContext]): F[List[VisionResponse]]
}
object VisionAPI {

  def apply[F[_]: Effect](
      implicit ME: MonadError[F, Throwable],
      EC: ExecutionContext): VisionAPI[F] = new VisionAPI[F] {
    type VisionResult[A] = EitherT[F, VisionError, A]

    def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient] =
      ME.catchNonFatal(settings.fold(ImageAnnotatorClient.create())(ImageAnnotatorClient.create))

    private def buildRequest(
        filePath: Either[String, ImageSource],
        featureType: Feature.Type,
        context: Option[ImageContext]): VisionResult[AnnotateImageRequest] = {

      def buildImage(filePath: Either[String, ImageSource]): VisionResult[Image] = {
        def getPath(path: String): F[Either[VisionError, Path]] =
          ErrorHandlerService.handleError(Paths.get(path), visionErrorHandler)

        val builder = Image.newBuilder
        filePath
          .fold(
            filePath =>
              EitherT(getPath(filePath))
                .map(path =>
                  builder.setContent(ByteString.copyFrom(Files.readAllBytes(path))).build()),
            source => EitherT.rightT[F, VisionError](builder.setSource(source).build())
          )
      }

      def buildFeature(featureType: Feature.Type): Feature =
        Feature.newBuilder.setType(featureType).build

      buildImage(filePath)
        .map(
          AnnotateImageRequest.newBuilder
            .addFeatures(buildFeature(featureType))
            .setImage(_)
        )
        .map(builder => context.map(builder.setImageContext).getOrElse(builder).build())
    }

    def labelImage(
        client: ImageAnnotatorClient,
        filePath: Either[String, ImageSource],
        context: Option[ImageContext]): F[VisionResponse] =
      (for {
        request  <- buildRequest(filePath, Type.LABEL_DETECTION, context)
        response <- EitherT(client.sendRequest(request)).subflatMap(_.getLabels)
      } yield response).value

    def labelImageBatch(
        client: ImageAnnotatorClient,
        fileList: List[Either[String, ImageSource]],
        context: Option[ImageContext]): F[List[VisionResponse]] = {

      def getBatchRequest: VisionResult[List[AnnotateImageRequest]] =
        fileList.traverse[VisionResult, AnnotateImageRequest](filePath =>
          buildRequest(filePath, Type.LABEL_DETECTION, context))

      (for {
        batchRequest <- getBatchRequest
        response     <- EitherT(client.sendRequestBatch(batchRequest))
      } yield response.getLabelsPerImage).fold(e => List(e.asLeft[List[VisionLabel]]), identity)
    }

  }
}
