package com.adrianrafo.gcp4s.vision
import java.nio.file._

import cats.data.EitherT
import cats.effect.Effect
import com.adrianrafo.gcp4s.ErrorHandlerService
import com.google.cloud.vision.v1._
import com.google.protobuf.ByteString

import scala.collection.JavaConverters._

object utils {

  def toBatchRequest(requests: List[AnnotateImageRequest]): BatchAnnotateImagesRequest =
    BatchAnnotateImagesRequest.newBuilder().addAllRequests(requests.asJava).build()

  def buildImageRequest[F[_]: Effect](
      filePath: Either[String, ImageSource],
      featureType: Feature.Type,
      context: Option[ImageContext],
      maxResults: Option[Int]): VisionResult[F, AnnotateImageRequest] = {

    def buildImage: VisionResult[F, Image] = {
      def getPath(path: String): EitherT[F, VisionError, Path] =
        ErrorHandlerService.handleError(Paths.get(path), visionErrorHandler)

      val builder = Image.newBuilder

      filePath
        .fold(
          filePath =>
            getPath(filePath)
              .map(path =>
                builder.setContent(ByteString.copyFrom(Files.readAllBytes(path))).build()),
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

}
