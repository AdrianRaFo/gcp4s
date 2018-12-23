package com.adrianrafo.gcp4s.vision

import cats.syntax.either._
import com.google.cloud.vision.v1.WebDetection.WebImage
import com.google.cloud.vision.v1._
import scala.collection.JavaConverters._
import scala.collection.mutable.Buffer

object ResponseHandler {

  def getPercentScore(score: Float): Int = (score * 100).toInt

  def handleErrors[A](
      response: AnnotateImageResponse,
      handleResponse: AnnotateImageResponse => A): Either[VisionError, A] =
    response match {
      case res if res.hasError => VisionError(s"Error: ${res.getError}").asLeft[A]
      case res                 => handleResponse(res).asRight[VisionError]
    }

  def buildCoordinates(boundingPoly: BoundingPoly, normalized: Boolean): VisionPosition = {

    def toVisionVertex(vertex: Vertex)                     = VisionVertex(vertex.getX.toFloat, vertex.getY.toFloat)
    def toNormalizedVisionVertex(vertex: NormalizedVertex) = VisionVertex(vertex.getX, vertex.getY)

    val vertices =
      if (normalized) boundingPoly.getNormalizedVerticesList.asScala.map(toNormalizedVisionVertex)
      else boundingPoly.getVerticesList.asScala.map(toVisionVertex)

    VisionPosition(vertices.toList)
  }

  def handleLabelResponse(res: AnnotateImageResponse): List[VisionLabel] =
    res.getLabelAnnotationsList.asScala.toList
      .map(tag => VisionLabel(tag.getDescription, getPercentScore(tag.getScore)))

  def handleTextResponse(res: AnnotateImageResponse): List[VisionText] =
    res.getTextAnnotationsList.asScala.toList.map(
      text =>
        VisionText(
          text.getDescription,
          text.getLocale,
          getPercentScore(text.getScore),
          buildCoordinates(text.getBoundingPoly, false)))

  def handleObjectResponse(res: AnnotateImageResponse): List[VisionObject] =
    res.getLocalizedObjectAnnotationsList.asScala.toList.map(
      entity =>
        VisionObject(
          entity.getName,
          getPercentScore(entity.getScore),
          buildCoordinates(entity.getBoundingPoly, true)))

  def handleFaceResponse(res: AnnotateImageResponse): List[VisionFace] =
    res.getFaceAnnotationsList.asScala.toList.map(
      annotation =>
        VisionFace(
          Grade.toGrade(annotation.getJoyLikelihoodValue),
          Grade.toGrade(annotation.getSurpriseLikelihoodValue),
          Grade.toGrade(annotation.getAngerLikelihoodValue),
          buildCoordinates(annotation.getBoundingPoly, true)
      ))

  def handleLogoResponse(res: AnnotateImageResponse): List[VisionLogo] =
    res.getLogoAnnotationsList.asScala.toList.map(
      annotation =>
        VisionLogo(
          annotation.getDescription,
          getPercentScore(annotation.getScore),
          buildCoordinates(annotation.getBoundingPoly, false)))

  def handleLandmarkResponse(res: AnnotateImageResponse): List[VisionLandMark] =
    res.getLandmarkAnnotationsList.asScala.toList.map { annotation =>
      val coord: List[VisionCoordinates] = annotation.getLocationsList.asScala.toList.map(
        locationInfo =>
          VisionCoordinates(
            locationInfo.getLatLng.getLatitude,
            locationInfo.getLatLng.getLongitude))

      VisionLandMark(
        annotation.getDescription,
        getPercentScore(annotation.getScore),
        VisionLocation(coord))
    }

  def handleSafeSearchResponse(res: AnnotateImageResponse): VisionSafeSearch = {
    val annotation = res.getSafeSearchAnnotation
    VisionSafeSearch(
      Grade.toGrade(annotation.getAdultValue),
      Grade.toGrade(annotation.getMedicalValue),
      Grade.toGrade(annotation.getSpoofValue),
      Grade.toGrade(annotation.getViolenceValue),
      Grade.toGrade(annotation.getRacyValue)
    )
  }

  def handleWebEntitiesResponse(res: AnnotateImageResponse): VisionWebDetection = {
    val annotation = res.getWebDetection

    def getImagesMatch(images: List[WebImage], level: MatchLevel.Value): List[VisionWebImageMatch] =
      images.map(image => VisionWebImageMatch(image.getUrl, getPercentScore(image.getScore), level))

    val entities: List[VisionWebEntity] = annotation.getWebEntitiesList.asScala.toList.map(entity =>
      VisionWebEntity(entity.getDescription, getPercentScore(entity.getScore)))

    val labels: List[VisionWebLabel] = annotation.getBestGuessLabelsList.asScala.toList.map(label =>
      VisionWebLabel(label.getLabel, label.getLanguageCode))

    val pages: List[VisionWebPageMatch] =
      annotation.getPagesWithMatchingImagesList.asScala.toList.map { page =>
        val partialImages =
          getImagesMatch(page.getPartialMatchingImagesList.asScala.toList, MatchLevel.Partial)
        val fullImages =
          getImagesMatch(page.getFullMatchingImagesList.asScala.toList, MatchLevel.Full)

        val images =
          (partialImages ++ fullImages).groupBy(_.url).mapValues(_.minBy(_.level)).values.toList
        VisionWebPageMatch(page.getPageTitle, page.getUrl, getPercentScore(page.getScore), images)
      }

    val partialImages: List[VisionWebImageMatch] =
      getImagesMatch(annotation.getPartialMatchingImagesList.asScala.toList, MatchLevel.Partial)
    val fullImages: List[VisionWebImageMatch] =
      getImagesMatch(annotation.getFullMatchingImagesList.asScala.toList, MatchLevel.Full)
    val similarImages: List[VisionWebImageMatch] =
      getImagesMatch(annotation.getVisuallySimilarImagesList.asScala.toList, MatchLevel.Similar)

    val images =
      (partialImages ++ fullImages ++ similarImages)
        .groupBy(_.url)
        .mapValues(_.minBy(_.level))
        .values
        .toList

    VisionWebDetection(entities, labels, pages, images)
  }

  def handleCropHintsResponse(res: AnnotateImageResponse) = ???
  /*
          CropHintsAnnotation annotation = res.getCropHintsAnnotation()
          for (CropHint hint : annotation.getCropHintsList()) {
            out.println(hint.getBoundingPoly())
          }
   */

  def handleDocumentTextResponse(res: AnnotateImageResponse) = ???
  /*
      TextAnnotation annotation = res.getFullTextAnnotation()
    for (Page page: annotation.getPagesList()) {
      String pageText = ""
      for (Block block : page.getBlocksList()) {
        String blockText = ""
        for (Paragraph para : block.getParagraphsList()) {
          String paraText = ""
          for (Word word: para.getWordsList()) {
            String wordText = ""
            for (Symbol symbol: word.getSymbolsList()) {
              wordText = wordText + symbol.getText()
              out.format("Symbol text: %s (confidence: %f)\n", symbol.getText(),
                symbol.getConfidence())
            }
            out.format("Word text: %s (confidence: %f)\n\n", wordText, word.getConfidence())
            paraText = String.format("%s %s", paraText, wordText)
          }
          // Output Example using Paragraph:
          out.println("\nParagraph: \n" + paraText)
          out.format("Paragraph Confidence: %f\n", para.getConfidence())
          blockText = blockText + paraText
        }
        pageText = pageText + blockText
      }
    }
    out.println("\nComplete annotation:")
    out.println(annotation.getText())
   */

  def handleImagePropertiesResponse(res: AnnotateImageResponse) = ???
  /*
          DominantColorsAnnotation colors = res.getImagePropertiesAnnotation().getDominantColors()
          for (ColorInfo color : colors.getColorsList()) {
            out.printf(
              "fraction: %f\nr: %f, g: %f, b: %f\n",
              color.getPixelFraction(),
              color.getColor().getRed(),
              color.getColor().getGreen(),
              color.getColor().getBlue())
 */

}
