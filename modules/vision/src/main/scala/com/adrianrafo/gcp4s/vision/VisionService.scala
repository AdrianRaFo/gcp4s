package com.adrianrafo.gcp4s.vision

import cats.syntax.either._
import java.nio.file._

import cats.MonadError
import cats.data.EitherT
import cats.instances.list._
import cats.instances.either._
import cats.syntax.functor._
import cats.syntax.traverse._
import cats.syntax.flatMap._
import cats.effect.Sync
import com.google.cloud.vision.v1._
import com.google.cloud.vision.v1.Feature.Type
import com.google.protobuf.ByteString

trait VisionService[F[_]] {
  def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient]

  def seeImage(
      client: ImageAnnotatorClient,
      filePath: Either[ImageSource, String],
      context: Option[ImageContext]): F[VisionResponse]

  def seeImageBatch(
      client: ImageAnnotatorClient,
      fileList: List[Either[ImageSource, String]],
      context: Option[ImageContext]): F[List[VisionResponse]]
}
object VisionService {

  def apply[F[_]: Sync](
      implicit ME: MonadError[F, Throwable],
      EHS: ErrorHandlerService[F]): VisionService[F] = new VisionService[F] {
    type VisionResult[A] = EitherT[F, VisionError, A]

    def createClient(settings: Option[ImageAnnotatorSettings]): F[ImageAnnotatorClient] =
      ME.catchNonFatal(settings.fold(ImageAnnotatorClient.create())(ImageAnnotatorClient.create))

    private def buildRequest(
        filePath: Either[ImageSource, String],
        featureType: Feature.Type,
        context: Option[ImageContext]): VisionResult[AnnotateImageRequest] = {

      def buildImage(filePath: Either[ImageSource, String]): VisionResult[Image] = {
        def getPath(path: String): F[Either[VisionError, Path]] = EHS.handleError(Paths.get(path))

        val builder = Image.newBuilder
        filePath
          .fold(
            source => EitherT.rightT[F, VisionError](builder.setSource(source).build()),
            filePath =>
              EitherT(getPath(filePath))
                .map(path =>
                  builder.setContent(ByteString.copyFrom(Files.readAllBytes(path))).build())
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

    def seeImage(
        client: ImageAnnotatorClient,
        filePath: Either[ImageSource, String],
        context: Option[ImageContext]): F[VisionResponse] =
      (for {
        request  <- buildRequest(filePath, Type.LABEL_DETECTION, context)
        response <- EitherT(client.labelImage(request)).subflatMap(_.getLabels)
      } yield response).value

    def seeImageBatch(
        client: ImageAnnotatorClient,
        fileList: List[Either[ImageSource, String]],
        context: Option[ImageContext]): F[List[VisionResponse]] = {

      def getBatchRequest: VisionResult[List[AnnotateImageRequest]] =
        fileList.traverse[VisionResult, AnnotateImageRequest](filePath =>
          buildRequest(filePath, Type.LABEL_DETECTION, context))

      (for {
        batchRequest <- getBatchRequest
        response     <- EitherT(client.labelImageBatch(batchRequest))
      } yield response.getLabelsPerImage).fold(e => List(e.asLeft[List[VisionLabel]]), identity)
    }

  }
}
