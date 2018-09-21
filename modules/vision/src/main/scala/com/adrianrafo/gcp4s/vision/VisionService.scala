package com.adrianrafo.gcp4s.vision

import cats.syntax.either._
import java.nio.file.{Files, Paths}
import com.google.cloud.vision.v1.Feature.Type
import com.google.cloud.vision.v1._
import com.google.protobuf.ByteString

class VisionService {

  def createClient(settings: Option[ImageAnnotatorSettings]): ImageAnnotatorClient =
    settings.fold(ImageAnnotatorClient.create())(ImageAnnotatorClient.create)

  def buildBasicImage(filePath: String): Image =
    Image.newBuilder
      .setContent(ByteString.copyFrom(Files.readAllBytes(Paths.get(filePath))))
      .build()

  def buildBasicFeature(featureType: Feature.Type): Feature =
    Feature.newBuilder.setType(featureType).build

  def buildBasicRequest(filePath: String, featureType: Feature.Type): AnnotateImageRequest =
    AnnotateImageRequest.newBuilder
      .addFeatures(buildBasicFeature(featureType))
      .setImage(buildBasicImage(filePath))
      .build

  def buildRequest(feature: Feature, image: Image): AnnotateImageRequest =
    AnnotateImageRequest.newBuilder.addFeatures(feature).setImage(image).build

  def basicSee(fileName: String): Either[VisionError, List[VisionLabel]] = {
    import implicits._
    val client   = createClient(None)
    val request  = buildBasicRequest(fileName, Type.LABEL_DETECTION)
    val response = client.annotateImage(request)
    response.getLabels
  }

  def see(
      client: ImageAnnotatorClient,
      request: AnnotateImageRequest): List[Either[VisionError, List[VisionLabel]]] = {
    import implicits._
    val response = client.annotateImage(request)
    response.getLabelsPerImage
  }

  sealed trait Implicits {
    final class ImageAnnotatorClientOps(client: ImageAnnotatorClient) {

      import scala.collection.JavaConverters._

      def annotateImage(requests: AnnotateImageRequest): BatchAnnotateImagesResponse =
        client.batchAnnotateImages(List(requests).asJava)

      def annotateImagesBatch(requests: List[AnnotateImageRequest]): BatchAnnotateImagesResponse =
        client.batchAnnotateImages(requests.asJava)

    }

    final class BatchAnnotateImagesResponseOps(response: BatchAnnotateImagesResponse) {

      import scala.collection.JavaConverters._

      private def getPercentScore(score: Float): Int = (score * 100).toInt

      def getLabelsPerImage: List[Either[VisionError, List[VisionLabel]]] = {
        response.getResponsesList.asScala
          .foldRight(List.empty[Either[VisionError, List[VisionLabel]]]) {
            case (res, list) if res.hasError =>
              list :+ VisionError(s"Error: ${res.getError}").asLeft[List[VisionLabel]]
            case (res, list) if !res.hasError =>
              list :+ res.getLabelAnnotationsList.asScala.toList
                .map(tag => VisionLabel(tag.getLocale, getPercentScore(tag.getScore)))
                .asRight[VisionError]
          }
      }

      def getLabels: Either[VisionError, List[VisionLabel]] = {
        response.getResponses(0) match {
          case res if res.hasError =>
            VisionError(s"Error: ${res.getError}").asLeft[List[VisionLabel]]
          case res if !res.hasError =>
            res.getLabelAnnotationsList.asScala.toList
              .map(tag => VisionLabel(tag.getLocale, getPercentScore(tag.getScore)))
              .asRight[VisionError]
        }
      }

    }

    implicit def imageAnnotatorClientOps(client: ImageAnnotatorClient): ImageAnnotatorClientOps =
      new ImageAnnotatorClientOps(client)

    implicit def batchAnnotateImagesResponseOps(
        response: BatchAnnotateImagesResponse): BatchAnnotateImagesResponseOps =
      new BatchAnnotateImagesResponseOps(response)

  }
  object implicits extends Implicits
}
