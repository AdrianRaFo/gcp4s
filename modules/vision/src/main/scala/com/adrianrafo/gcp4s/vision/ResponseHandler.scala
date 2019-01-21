package com.adrianrafo.gcp4s.vision

import com.google.cloud.vision.v1.TextAnnotation.TextProperty
import com.google.cloud.vision.v1.WebDetection.WebImage
import com.google.cloud.vision.v1._

import scala.collection.JavaConverters._

object ResponseHandler {

  private def getConfidence(score: Float): Int = (score * 100).toInt

  private def getPosition(boundingPoly: BoundingPoly, normalized: Boolean): VisionPosition = {

    def toVisionVertex(vertex: Vertex)                     = VisionVertex(vertex.getX.toFloat, vertex.getY.toFloat)
    def toNormalizedVisionVertex(vertex: NormalizedVertex) = VisionVertex(vertex.getX, vertex.getY)

    val vertices =
      if (normalized) boundingPoly.getNormalizedVerticesList.asScala.map(toNormalizedVisionVertex)
      else boundingPoly.getVerticesList.asScala.map(toVisionVertex)

    VisionPosition(vertices.toList)
  }

  def handleLabelResponse(res: AnnotateImageResponse): VisionLabelResponse = {
    val labels = res.getLabelAnnotationsList.asScala.toList
      .map(tag => VisionLabel(tag.getDescription, getConfidence(tag.getScore)))

    VisionLabelResponse(labels)
  }

  def handleTextResponse(res: AnnotateImageResponse): VisionTextResponse = {
    val texts = res.getTextAnnotationsList.asScala.toList.map(
      text =>
        VisionText(
          text.getDescription,
          text.getLocale,
          getConfidence(text.getScore),
          getPosition(text.getBoundingPoly, false)))

    VisionTextResponse(texts)
  }

  def handleObjectResponse(res: AnnotateImageResponse): VisionObjectResponse = {
    val objects = res.getLocalizedObjectAnnotationsList.asScala.toList.map(
      entity =>
        VisionObject(
          entity.getName,
          getConfidence(entity.getScore),
          getPosition(entity.getBoundingPoly, true)))

    VisionObjectResponse(objects)
  }

  def handleFaceResponse(res: AnnotateImageResponse): VisionFaceResponse = {
    val faces = res.getFaceAnnotationsList.asScala.toList.map(
      annotation =>
        VisionFace(
          Grade.fromValue(annotation.getJoyLikelihoodValue),
          Grade.fromValue(annotation.getSurpriseLikelihoodValue),
          Grade.fromValue(annotation.getAngerLikelihoodValue),
          getPosition(annotation.getBoundingPoly, true)
      ))

    VisionFaceResponse(faces)
  }

  def handleLogoResponse(res: AnnotateImageResponse): VisionLogoResponse = {
    val logos = res.getLogoAnnotationsList.asScala.toList.map(
      annotation =>
        VisionLogo(
          annotation.getDescription,
          getConfidence(annotation.getScore),
          getPosition(annotation.getBoundingPoly, false)))

    VisionLogoResponse(logos)
  }

  def handleLandmarkResponse(res: AnnotateImageResponse): VisionLandMarkResponse = {
    val landmarks = res.getLandmarkAnnotationsList.asScala.toList.map { annotation =>
      val coord: List[VisionCoordinates] = annotation.getLocationsList.asScala.toList.map(
        locationInfo =>
          VisionCoordinates(
            locationInfo.getLatLng.getLatitude,
            locationInfo.getLatLng.getLongitude))

      VisionLandMark(
        annotation.getDescription,
        getConfidence(annotation.getScore),
        VisionLocation(coord))
    }

    VisionLandMarkResponse(landmarks)
  }

  def handleSafeSearchResponse(res: AnnotateImageResponse): VisionSafeSearch = {
    val annotation = res.getSafeSearchAnnotation
    VisionSafeSearch(
      Grade.fromValue(annotation.getAdultValue),
      Grade.fromValue(annotation.getMedicalValue),
      Grade.fromValue(annotation.getSpoofValue),
      Grade.fromValue(annotation.getViolenceValue),
      Grade.fromValue(annotation.getRacyValue)
    )
  }

  def handleWebEntitiesResponse(res: AnnotateImageResponse): VisionWebDetection = {
    val annotation = res.getWebDetection

    def getImagesMatch(images: List[WebImage], level: MatchLevel.Value): List[VisionWebImageMatch] =
      images.map(image => VisionWebImageMatch(image.getUrl, getConfidence(image.getScore), level))

    val entities: List[VisionWebEntity] = annotation.getWebEntitiesList.asScala.toList.map(entity =>
      VisionWebEntity(entity.getDescription, getConfidence(entity.getScore)))

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
        VisionWebPageMatch(page.getPageTitle, page.getUrl, getConfidence(page.getScore), images)
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

  def handleCropHintResponse(res: AnnotateImageResponse): VisionCropHintResponse = {
    val cropHints = res.getCropHintsAnnotation.getCropHintsList.asScala.toList.map(
      cropHint =>
        VisionCropHint(
          getPosition(cropHint.getBoundingPoly, false),
          getConfidence(cropHint.getConfidence),
          cropHint.getImportanceFraction))

    VisionCropHintResponse(cropHints)
  }

  def handleDocumentTextResponse(res: AnnotateImageResponse): VisionDocument = {

    def getDetectedLanguages(property: TextProperty): List[VisionLanguage] =
      property.getDetectedLanguagesList.asScala.toList.map(language =>
        VisionLanguage(language.getLanguageCode, getConfidence(language.getConfidence)))

    val annotation = res.getFullTextAnnotation

    val pages: List[VisionPage] = annotation.getPagesList.asScala.toList.map { page =>
      val blocks: List[VisionBlock] = page.getBlocksList.asScala.toList.map { block =>
        val paragraphs: List[VisionParagraph] = block.getParagraphsList.asScala.toList.map {
          paragraph =>
            val words: List[VisionWord] = paragraph.getWordsList.asScala.toList.map { word =>
              val text = word.getSymbolsList.asScala.map(_.getText).mkString
              VisionWord(
                text,
                getDetectedLanguages(word.getProperty),
                getConfidence(word.getConfidence),
                getPosition(word.getBoundingBox, false))
            }
            VisionParagraph(
              words.map(_.text).mkString,
              getConfidence(paragraph.getConfidence),
              getDetectedLanguages(paragraph.getProperty),
              words)
        }
        VisionBlock(
          paragraphs.map(_.text).mkString,
          getConfidence(block.getConfidence),
          getDetectedLanguages(block.getProperty),
          paragraphs)
      }
      VisionPage(
        blocks.map(_.text).mkString,
        getConfidence(page.getConfidence),
        getDetectedLanguages(page.getProperty),
        page.getWidth,
        page.getHeight,
        blocks)
    }

    VisionDocument(annotation.getText, pages)
  }

  def handleImagePropertiesResponse(res: AnnotateImageResponse): VisionImageProperties = {
    val imgProperties = res.getImagePropertiesAnnotation

    val colors: List[VisionColor] = if (imgProperties.hasDominantColors) {
      imgProperties.getDominantColors.getColorsList.asScala.toList.map(
        color =>
          VisionColor(
            color.getColor.getRed,
            color.getColor.getGreen,
            color.getColor.getBlue,
            color.getColor.getAlpha.getValue,
            color.getPixelFraction,
            getConfidence(color.getScore)
        )
      )
    } else List.empty

    VisionImageProperties(colors)
  }

}
