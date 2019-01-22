package com.adrianrafo.gcp4s.vision

import com.google.cloud.vision.v1.Likelihood

//Common
case class VisionError(message: String)

case class VisionVertex(x: Float, y: Float)

case class VisionPosition(vertices: List[VisionVertex])

case class VisionCoordinates(latitude: Double, longitude: Double)

case class VisionLocation(coordinates: List[VisionCoordinates])

object Grade extends Enumeration {
  type Grade = Value
  val Unknown, VeryUnlikely, Unlikely, Possible, Likely, VeryLikely = Value

  def fromValue(likelihood: Int): Grade.Value = Likelihood.forNumber(likelihood) match {
    case Likelihood.VERY_UNLIKELY => VeryUnlikely
    case Likelihood.UNLIKELY      => Unlikely
    case Likelihood.POSSIBLE      => Possible
    case Likelihood.LIKELY        => Likely
    case Likelihood.VERY_LIKELY   => VeryLikely
    case _                        => Unknown
  }
}

//Label
case class VisionLabel(label: String, confidence: Int)

case class VisionLabelResponse(labels: List[VisionLabel])

//Text
case class VisionText(text: String, locale: String, confidence: Int, position: VisionPosition)

case class VisionTextResponse(texts: List[VisionText])

//Document
case class VisionLanguage(code: String, confidence: Int)

case class VisionWord(
    text: String,
    languages: List[VisionLanguage],
    confidence: Int,
    position: VisionPosition)

case class VisionParagraph(
    text: String,
    confidence: Int,
    languages: List[VisionLanguage],
    words: List[VisionWord])

case class VisionBlock(
    text: String,
    confidence: Int,
    languages: List[VisionLanguage],
    paragraphs: List[VisionParagraph])

case class VisionPage(
    text: String,
    confidence: Int,
    languages: List[VisionLanguage],
    width: Int,
    height: Int,
    blocks: List[VisionBlock])

case class VisionDocument(text: String, pages: List[VisionPage])

//Object
case class VisionObject(name: String, confidence: Int, position: VisionPosition)

case class VisionObjectResponse(objects: List[VisionObject])
//Face
case class VisionFace(
    joy: Grade.Value,
    surprise: Grade.Value,
    anger: Grade.Value,
    position: VisionPosition
)

case class VisionFaceResponse(faces: List[VisionFace])

//Logo
case class VisionLogo(description: String, confidence: Int, position: VisionPosition)

case class VisionLogoResponse(logos: List[VisionLogo])

//Landmark
case class VisionLandMark(description: String, confidence: Int, location: VisionLocation)

case class VisionLandMarkResponse(landmarks: List[VisionLandMark])

//Safe search
case class VisionSafeSearch(
    adult: Grade.Value,
    spoof: Grade.Value,
    medical: Grade.Value,
    violence: Grade.Value,
    racy: Grade.Value)

//Web
object MatchLevel extends Enumeration {
  type MatchLevel = Value
  val Full, Partial, Similar = Value
}

case class VisionWebImageMatch(url: String, confidence: Int, level: MatchLevel.Value)

case class VisionWebPageMatch(
    title: String,
    url: String,
    confidence: Int,
    images: List[VisionWebImageMatch])

case class VisionWebEntity(description: String, confidence: Int)

case class VisionWebLabel(label: String, code: String)

case class VisionWebDetection(
    entities: List[VisionWebEntity],
    webLabels: List[VisionWebLabel],
    pages: List[VisionWebPageMatch],
    images: List[VisionWebImageMatch])

//Crop hints
case class VisionCropHint(position: VisionPosition, confidence: Int, importanceFraction: Float)

case class VisionCropHintResponse(cropHints: List[VisionCropHint])

//Image Properties
case class VisionColor(
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float,
    pixelFraction: Float,
    confidence: Int)

case class VisionImageProperties(dominantColors: List[VisionColor])
