package com.adrianrafo.gcp4s.vision

import cats.Show
import cats.derived.semi
import cats.instances.all._
import com.google.cloud.vision.v1.Likelihood

//Common

case class VisionError(message: String)

object VisionError {
  implicit val VisionErrorShow: Show[VisionError] = semi.show[VisionError]
}

case class VisionVertex(x: Float, y: Float)

object VisionVertex {
  implicit val VisionVertexShow: Show[VisionVertex] = semi.show[VisionVertex]
}

case class PositionSquare(
  leftDown: VisionVertex,
  rightDown: VisionVertex,
  rightTop: VisionVertex,
  leftTop: VisionVertex
)

object PositionSquare {
  implicit val PositionSquareShow: Show[PositionSquare] =
    Show.show[PositionSquare]{sqr =>
    import sqr._
      s"""PositionSquare:
         |  ${leftTop.x}, ${leftTop.y} - ${rightTop.x}, ${rightTop.y}
         |  ${leftDown.x}, ${leftDown.y} - ${rightDown.x}, ${rightDown.y}""".stripMargin}
}

case class VisionPosition(vertices: List[VisionVertex]) {
  def asSquare: Option[PositionSquare] =
    if (vertices.size == 4) {
      Some(PositionSquare(
        leftDown = vertices.head,
        rightDown = vertices.tail.head,
        rightTop = vertices.tail.tail.head,
        leftTop = vertices.last
      ))
    } else None
}

object VisionPosition {
  implicit val VisionPositionShow: Show[VisionPosition] = semi.show[VisionPosition]
}

case class VisionCoordinates(latitude: Double, longitude: Double)

object VisionCoordinates {
  implicit val VisionCoordinatesShow: Show[VisionCoordinates] = semi.show[VisionCoordinates]
}

case class VisionLocation(coordinates: List[VisionCoordinates])

object VisionLocation {
  implicit val VisionLocationShow: Show[VisionLocation] = semi.show[VisionLocation]
}

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

  implicit val GradeShow: Show[Grade.Value] = Show.fromToString[Grade.Value]
}

//Label
case class VisionLabel(label: String, confidence: Int)

object VisionLabel {
  implicit val visionLabelShow: Show[VisionLabel] = semi.show[VisionLabel]
}

case class VisionLabelResponse(labels: List[VisionLabel])

object VisionLabelResponse {
  implicit val visionLabelResponseShow: Show[VisionLabelResponse] = semi.show[VisionLabelResponse]
}

//Text
case class VisionText(text: String, locale: String, confidence: Int, position: VisionPosition)

object VisionText {
  implicit val visionTextShow: Show[VisionText] = semi.show[VisionText]
}

case class VisionTextResponse(texts: List[VisionText])

object VisionTextResponse {
  implicit val visionTextResponseShow: Show[VisionTextResponse] = semi.show[VisionTextResponse]
}

//Document
case class VisionLanguage(code: String, confidence: Int)

object VisionLanguage {
  implicit val visionLanguageShow: Show[VisionLanguage] = semi.show[VisionLanguage]
}

case class VisionWord(
  text: String,
  languages: List[VisionLanguage],
  confidence: Int,
  position: VisionPosition
)

object VisionWord {
  implicit val visionWordShow: Show[VisionWord] = semi.show[VisionWord]
}

case class VisionParagraph(
  text: String,
  confidence: Int,
  languages: List[VisionLanguage],
  words: List[VisionWord]
)

object VisionParagraph {
  implicit val visionParagraphShow: Show[VisionParagraph] = semi.show[VisionParagraph]
}

case class VisionBlock(
  text: String,
  confidence: Int,
  languages: List[VisionLanguage],
  paragraphs: List[VisionParagraph]
)

object VisionBlock {
  implicit val visionBlockShow: Show[VisionBlock] = semi.show[VisionBlock]
}

case class VisionPage(
  text: String,
  confidence: Int,
  languages: List[VisionLanguage],
  width: Int,
  height: Int,
  blocks: List[VisionBlock]
)

object VisionPage {
  implicit val visionPageShow: Show[VisionPage] = semi.show[VisionPage]
}

case class VisionDocument(text: String, pages: List[VisionPage])

object VisionDocument {
  implicit val visionDocumentShow: Show[VisionDocument] = semi.show[VisionDocument]
}

//Object
case class VisionObject(name: String, confidence: Int, position: VisionPosition)

object VisionObject {
  implicit val visionObjectShow: Show[VisionObject] = semi.show[VisionObject]
}

case class VisionObjectResponse(objects: List[VisionObject])

object VisionObjectResponse {
  implicit val vorShow: Show[VisionObjectResponse] = semi.show[VisionObjectResponse]
}

//Face
case class VisionFace(
  joy: Grade.Value,
  surprise: Grade.Value,
  anger: Grade.Value,
  sorrow: Grade.Value,
  position: VisionPosition
) {
  def isExpressionless: Boolean  = joy == surprise && surprise == anger && anger == sorrow
  def isMainlyJoy: Boolean       = !isExpressionless && joy >= surprise && joy >= anger && joy >= sorrow
  def isMainlySurprised: Boolean = !isExpressionless && surprise >= joy && surprise >= anger && surprise >= sorrow
  def isMainlyAnger: Boolean     = !isExpressionless && anger >= joy && anger >= surprise && anger >= sorrow
  def isMainlySorrowed: Boolean  = !isExpressionless && sorrow >= joy && sorrow >= surprise && sorrow >= anger
}

object VisionFace {
  implicit val visionFaceShow: Show[VisionFace] = semi.show[VisionFace]
}

case class VisionFaceResponse(faces: List[VisionFace])

object VisionFaceResponse {
  implicit val visionFaceResponseShow: Show[VisionFaceResponse] = semi.show[VisionFaceResponse]
}

//Logo
case class VisionLogo(description: String, confidence: Int, position: VisionPosition)

object VisionLogo {
  implicit val visionLogoShow: Show[VisionLogo] = semi.show[VisionLogo]
}

case class VisionLogoResponse(logos: List[VisionLogo])

object VisionLogoResponse {
  implicit val visionLogoResponseShow: Show[VisionLogoResponse] = semi.show[VisionLogoResponse]
}

//Landmark
case class VisionLandMark(description: String, confidence: Int, location: VisionLocation)

object VisionLandMark {
  implicit val visionLandMarkShow: Show[VisionLandMark] = semi.show[VisionLandMark]
}

case class VisionLandMarkResponse(landmarks: List[VisionLandMark])

object VisionLandMarkResponse {
  implicit val vlmrShow: Show[VisionLandMarkResponse] = semi.show[VisionLandMarkResponse]
}

//Safe search
case class VisionSafeSearch(
  adult: Grade.Value,
  spoof: Grade.Value,
  medical: Grade.Value,
  violence: Grade.Value,
  racy: Grade.Value
)

object VisionSafeSearch {
  implicit val visionSafeSearchShow: Show[VisionSafeSearch] = semi.show[VisionSafeSearch]
}

//Web
object MatchLevel extends Enumeration {
  type MatchLevel = Value
  val Full, Partial, Similar = Value

  implicit val MatchLevelShow: Show[MatchLevel.Value] = Show.fromToString[MatchLevel.Value]
}

case class VisionWebImageMatch(url: String, confidence: Int, level: MatchLevel.Value)

object VisionWebImageMatch {
  implicit val visionWebImageMatchShow: Show[VisionWebImageMatch] = semi.show[VisionWebImageMatch]

}

case class VisionWebPageMatch(
  title: String,
  url: String,
  confidence: Int,
  images: List[VisionWebImageMatch]
)

object VisionWebPageMatch {
  implicit val visionWebPageMatchShow: Show[VisionWebPageMatch] = semi.show[VisionWebPageMatch]
}

case class VisionWebEntity(description: String, confidence: Int)

object VisionWebEntity {
  implicit val visionWebEntityShow: Show[VisionWebEntity] = semi.show[VisionWebEntity]
}

case class VisionWebLabel(label: String, code: String)

object VisionWebLabel {
  implicit val visionWebLabelShow: Show[VisionWebLabel] = semi.show[VisionWebLabel]
}

case class VisionWebDetection(
  entities: List[VisionWebEntity],
  webLabels: List[VisionWebLabel],
  pages: List[VisionWebPageMatch],
  images: List[VisionWebImageMatch]
)

object VisionWebDetection {
  implicit val visionWebDetectionShow: Show[VisionWebDetection] = semi.show[VisionWebDetection]
}

//Crop hints
case class VisionCropHint(position: VisionPosition, confidence: Int, importanceFraction: Float)

object VisionCropHint {
  implicit val visionCropHintShow: Show[VisionCropHint] = semi.show[VisionCropHint]
}

case class VisionCropHintResponse(cropHints: List[VisionCropHint])

object VisionCropHintResponse {
  implicit val vchrShow: Show[VisionCropHintResponse] = semi.show[VisionCropHintResponse]
}

//Image Properties
case class VisionColor(
  red: Float,
  green: Float,
  blue: Float,
  alpha: Float,
  pixelFraction: Float,
  confidence: Int
)

object VisionColor {
  implicit val visionColorShow: Show[VisionColor] = semi.show[VisionColor]
}

case class VisionImageProperties(dominantColors: List[VisionColor])

object VisionImageProperties {
  implicit val vipShow: Show[VisionImageProperties] = semi.show[VisionImageProperties]
}
