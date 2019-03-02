package com.adrianrafo.gcp4s.vision.internal

import java.nio.file._

import cats.data.EitherT
import cats.effect.Effect
import cats.instances.option._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.syntax.traverse._
import com.adrianrafo.gcp4s.ErrorHandlerService
import com.adrianrafo.gcp4s.vision._
import com.google.cloud.vision.v1._
import com.google.protobuf.ByteString

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

private[vision] object RequestBuilder {

  def toBatchRequest(requests: List[AnnotateImageRequest]): BatchAnnotateImagesRequest =
    BatchAnnotateImagesRequest.newBuilder().addAllRequests(requests.asJava).build()

  def buildImageRequest[F[_]](
      filePath: VisionSource,
      featureType: Feature.Type,
      context: Option[ImageContext],
      maxResults: Option[Int])(
      implicit E: Effect[F],
      EC: ExecutionContext): VisionResult[F, AnnotateImageRequest] = {

    def buildImage: VisionResult[F, Image] = {
      def getPath(path: String): VisionResult[F, Path] =
        ErrorHandlerService.handleError(Paths.get(path), visionErrorHandler)

      val builder = Image.newBuilder

      filePath
        .fold(
          filePath =>
            getPath(filePath)
              .flatMap(path =>
                ErrorHandlerService
                  .handleError(ByteString.copyFrom(Files.readAllBytes(path)), visionErrorHandler))
              .map(builder.setContent(_).build()),
          source => EitherT.rightT[F, VisionError](builder.setSource(source).build())
        )
    }

    def buildFeature: Feature = {
      val builder = Feature.newBuilder.setType(featureType)
      maxResults.fold(builder)(builder.setMaxResults).build
    }

    buildImage
      .map(
        AnnotateImageRequest.newBuilder
          .addFeatures(buildFeature)
          .setImage(_)
      )
      .map(builder => context.map(builder.setImageContext).getOrElse(builder).build())
  }

  def createTextDetectionContext[F[_]](languages: Option[List[String]])(
      implicit E: Effect[F]): F[Option[ImageContext]] =
    languages.traverse(lang => createImageContext(_.addAllLanguageHints(lang.asJava)))

  def createWebDetectionContext[F[_]](includeGeoLocation: Boolean)(
      implicit E: Effect[F]): F[Option[ImageContext]] =
    E.delay(WebDetectionParams.newBuilder().setIncludeGeoResults(includeGeoLocation).build())
      .flatMap(params => createImageContext(_.setWebDetectionParams(params)))
      .map(_.some)

  def createCropHintContext[F[_]](aspectRatios: Option[List[Float]])(
      implicit E: Effect[F]): F[Option[ImageContext]] =
    aspectRatios.traverse(
      ratios =>
        E.delay(
            CropHintsParams.newBuilder().addAllAspectRatios(ratios.map(float2Float).asJava).build())
          .flatMap(params => createImageContext(_.setCropHintsParams(params))))

  private def createImageContext[F[_]](contextParams: ImageContext.Builder => ImageContext.Builder)(
      implicit E: Effect[F]): F[ImageContext] =
    E.delay(contextParams(ImageContext.newBuilder()).build())
}
