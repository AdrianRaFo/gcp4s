package com.adrianrafo.gcp4s.vision

import cats.syntax.either._
import com.google.cloud.vision.v1._

import scala.collection.JavaConverters._

object ResponseHandler {

  def getPercentScore(score: Float): Int = (score * 100).toInt

  def handleErrors[A](
      response: AnnotateImageResponse,
      handleResponse: AnnotateImageResponse => A): Either[VisionError, A] =
    response match {
      case res if res.hasError => VisionError(s"Error: ${res.getError}").asLeft[A]
      case res                 => handleResponse(res).asRight[VisionError]
    }

  def handleLabelResponse(res: AnnotateImageResponse): List[VisionLabel] =
    res.getLabelAnnotationsList.asScala.toList
      .map(tag => VisionLabel(tag.getDescription, getPercentScore(tag.getScore)))

  def handleTextResponse(res: AnnotateImageResponse) = {

    def buildCoordinates(boundingPoly: BoundingPoly): VisionCoordinates = {
      val vertex      = boundingPoly.getVerticesList
      val bottomLeft  = VisionVertex(vertex.get(0).getX, vertex.get(0).getY)
      val topLeft     = VisionVertex(vertex.get(0).getX, vertex.get(0).getY)
      val topRight    = VisionVertex(vertex.get(0).getX, vertex.get(0).getY)
      val bottomRight = VisionVertex(vertex.get(0).getX, vertex.get(0).getY)
      VisionCoordinates(bottomLeft, topLeft, topRight, bottomRight)
    }

    res.getTextAnnotationsList.asScala.toList.map(
      text =>
        VisionText(
          text.getDescription,
          text.getLocale,
          getPercentScore(text.getScore),
          buildCoordinates(text.getBoundingPoly)))
  }

}
