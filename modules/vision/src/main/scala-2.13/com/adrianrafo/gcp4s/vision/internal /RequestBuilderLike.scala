package com.adrianrafo.gcp4s.vision.internal

import cats.effect.Effect
import cats.instances.option._
import cats.syntax.flatMap._
import cats.syntax.traverse._
import com.google.cloud.vision.v1._

import scala.jdk.CollectionConverters._

private[internal] trait RequestBuilderLike {

  def toBatchRequest(requests: List[AnnotateImageRequest]): BatchAnnotateImagesRequest =
    BatchAnnotateImagesRequest.newBuilder().addAllRequests(requests.asJava).build()

  def createTextDetectionContext[F[_]](
    languages: Option[List[String]]
  )(implicit E: Effect[F]): F[Option[ImageContext]] =
    languages.traverse(lang => createImageContext(_.addAllLanguageHints(lang.asJava)))

  def createCropHintContext[F[_]](
    aspectRatios: Option[List[Float]]
  )(implicit E: Effect[F]): F[Option[ImageContext]] =
    aspectRatios.traverse(ratios =>
      E.delay(CropHintsParams.newBuilder().addAllAspectRatios(ratios.map(float2Float).asJava).build())
        .flatMap(params => createImageContext(_.setCropHintsParams(params)))
    )

  protected def createImageContext[F[_]](
    contextParams: ImageContext.Builder => ImageContext.Builder
  )(implicit E: Effect[F]): F[ImageContext] =
    E.delay(contextParams(ImageContext.newBuilder()).build())

}
