package com.adrianrafo.gcp4s.vision.internal

import java.nio.file._

import cats.data.EitherT
import cats.effect.Effect
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import com.adrianrafo.gcp4s.ErrorHandlerService
import com.adrianrafo.gcp4s.vision._
import com.google.cloud.vision.v1._
import com.google.protobuf.ByteString

import scala.concurrent.ExecutionContext

private[vision] object RequestBuilder extends RequestBuilderLike {

  def buildImageRequest[F[_]](
    filePath: VisionSource,
    featureType: Feature.Type,
    context: Option[ImageContext],
    maxResults: Option[Int]
  )(implicit E: Effect[F], EC: ExecutionContext): VisionResult[F, AnnotateImageRequest] = {

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
                  .handleError(ByteString.copyFrom(Files.readAllBytes(path)), visionErrorHandler)
              )
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

  def createWebDetectionContext[F[_]](
    includeGeoLocation: Boolean
  )(implicit E: Effect[F]): F[Option[ImageContext]] =
    E.delay(WebDetectionParams.newBuilder().setIncludeGeoResults(includeGeoLocation).build())
      .flatMap(params => createImageContext(_.setWebDetectionParams(params)))
      .map(_.some)

}
