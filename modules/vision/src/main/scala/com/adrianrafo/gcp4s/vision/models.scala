package com.adrianrafo.gcp4s.vision
import com.google.cloud.vision.v1.Likelihood

case class VisionError(message: String)

case class VisionVertex(x: Float, y: Float)

case class VisionPosition(vertices: List[VisionVertex])

case class VisionCoordinates(latitude: Double, longitude: Double)

case class VisionLocation(coordinates: List[VisionCoordinates])

case class VisionLabel(label: String, confidence: Int)

case class VisionText(text: String, locale: String, confidence: Int, position: VisionPosition)

case class VisionParagraph(
    text: String,
    confidence: Int,
    locale: List[String],
    words: List[VisionText])

case class VisionBlock(
    text: String,
    confidence: Int,
    locale: List[String],
    paragraphs: List[VisionParagraph])

case class VisionPage(
    text: String,
    confidence: Int,
    locale: List[String],
    blocks: List[VisionBlock])

case class VisionDocument(
    text: String,
    confidence: Int,
    locale: List[String],
    pages: List[VisionPage])

object Grade extends Enumeration {
  type Grade = Value
  val Unknown, VeryUnlikely, Unlikely, Possible, Likely, VeryLikely = Value

  def toGrade(likelihood: Int): Grade.Value = Likelihood.forNumber(likelihood) match {
    case Likelihood.VERY_UNLIKELY => VeryUnlikely
    case Likelihood.UNLIKELY      => Unlikely
    case Likelihood.POSSIBLE      => Possible
    case Likelihood.LIKELY        => Likely
    case Likelihood.VERY_LIKELY   => VeryLikely
    case _                        => Unknown
  }
}

case class VisionObject(name: String, confidence: Int, position: VisionPosition)

case class VisionFace(
    joy: Grade.Value,
    surprise: Grade.Value,
    anger: Grade.Value,
    position: VisionPosition
)

case class VisionLogo(description: String, confidence: Int, position: VisionPosition)

case class VisionLandMark(description: String, confidence: Int, location: VisionLocation)

case class VisionSafeSearch(
    adult: Grade.Value,
    spoof: Grade.Value,
    medical: Grade.Value,
    violence: Grade.Value,
    racy: Grade.Value)

object MatchLevel extends Enumeration {
  type MatchLevel = Value
  val Full, Partial, Similar = Value
  }

case class VisionWebImageMatch(url:String, confidence:Int, level: MatchLevel.Value)

case class VisionWebPageMatch(title:String, url: String,  confidence:Int, images: List[VisionWebImageMatch])

case class VisionWebEntity(description:String, confidence:Int)

case class VisionWebLabel(label:String, code:String)

case class VisionWebDetection(entities: List[VisionWebEntity], labels:List[VisionWebLabel], pages: List[VisionWebPageMatch], images: List[VisionWebImageMatch])

case class VisionCropHints(position: VisionPosition, confidence: Int, importanceFraction: Double)

case class VisionImageProperties()
