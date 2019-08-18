package com.adrianrafo.gcp4s.vision

import java.net.URI

import cats.effect.{Effect, Resource}
import cats.syntax.applicative._
import cats.syntax.functor._
import com.adrianrafo.gcp4s.{Gcp4sClient, Gcp4sCredentialsProvider}
import com.google.cloud.vision.v1._

final class VisionClient[F[_]](settings: ImageAnnotatorSettings)(
    implicit E: Effect[F],
    visionAPI: VisionAPI[F]
) extends Gcp4sClient[F, ImageAnnotatorClient] {

  private[gcp4s] val client: F[ImageAnnotatorClient] =
    E.catchNonFatal(ImageAnnotatorClient.create(settings))

  def labelImage(
      maxResults: Option[Int],
      fileList: VisionSource*
  ): F[VisionResponse[VisionLabelResponse]] =
    visionAPI.labelImage(this, maxResults, fileList: _*)

  /**
   * To detect handwritten text add "en-t-i0-handwrit" language hint
   * None for autodetect language
   */
  def textDetection(
      languages: Option[List[String]],
      fileList: VisionSource*
  ): F[VisionResponse[VisionTextResponse]] =
    visionAPI.textDetection(this, languages, fileList: _*)

  /**
   * To detect handwritten text add "en-t-i0-handwrit" language hint
   * None for autodetect language
   */
  def documentTextDetection(
      languages: Option[List[String]],
      fileList: VisionSource*
  ): F[VisionResponse[VisionDocument]] =
    visionAPI.documentTextDetection(this, languages, fileList: _*)

  def faceDetection(
      maxResults: Option[Int],
      fileList: VisionSource*
  ): F[VisionResponse[VisionFaceResponse]] =
    visionAPI.faceDetection(this, maxResults, fileList: _*)

  def logoDetection(
      maxResults: Option[Int],
      fileList: VisionSource*
  ): F[VisionResponse[VisionLogoResponse]] =
    visionAPI.logoDetection(this, maxResults, fileList: _*)

  /**
   * Aspect ratios in floats, representing the ratio of the width to the height
   * of the image. For example, if the desired aspect ratio is 4/3, the
   * corresponding float value should be 1.33333.  If not specified, the
   * best possible crop is returned. The number of provided aspect ratios is
   * limited to a maximum of 16; any aspect ratios provided after the 16th are
   * ignored.
   *
   */
  def cropHints(
      aspectRatios: Option[List[Float]],
      fileList: VisionSource*
  ): F[VisionResponse[VisionCropHintResponse]] =
    visionAPI.cropHints(this, aspectRatios, fileList: _*)

  def landmarkDetection(
      maxResults: Option[Int],
      fileList: VisionSource*
  ): F[VisionResponse[VisionLandMarkResponse]] =
    visionAPI.landmarkDetection(this, maxResults, fileList: _*)

  def imagePropertiesDetection(fileList: VisionSource*): F[VisionResponse[VisionImageProperties]] =
    visionAPI.imagePropertiesDetection(this, fileList: _*)

  def safeSearchDetection(fileList: VisionSource*): F[VisionResponse[VisionSafeSearch]] =
    visionAPI.safeSearchDetection(this, fileList: _*)

  def webEntitiesDetection(
      includeGeoLocation: Boolean,
      maxResults: Option[Int],
      fileList: VisionSource*
  ): F[VisionResponse[VisionWebDetection]] =
    visionAPI.webEntitiesDetection(this, includeGeoLocation, maxResults, fileList: _*)

  def objectDetection(
      maxResults: Option[Int],
      fileList: VisionSource*
  ): F[VisionResponse[VisionObjectResponse]] =
    visionAPI.objectDetection(this, maxResults, fileList: _*)

}

object VisionClient {

  private[vision] def buildSettings[F[_]: Effect](
      maybeCredentialsProvider: Option[Gcp4sCredentialsProvider[F]]
  ): F[ImageAnnotatorSettings] = {
    val builder = ImageAnnotatorSettings.newBuilder()
    maybeCredentialsProvider
      .fold(builder.pure[F])(cr => cr.credentials.map(builder.setCredentialsProvider))
      .map(_.build())
  }

  def createClient[F[_]](
      maybeCredentialsProvider: Option[Gcp4sCredentialsProvider[F]] = None
  )(implicit E: Effect[F], visionAPI: VisionAPI[F]): Resource[F, VisionClient[F]] =
    Resource.make(VisionClient.buildSettings(maybeCredentialsProvider).map(new VisionClient(_)))(
      _.shutdown()
    )

  def createImageSource[F[_]](uri: URI)(implicit E: Effect[F]): F[ImageSource] =
    E.delay(ImageSource.newBuilder().setImageUri(uri.toString).build())

}
