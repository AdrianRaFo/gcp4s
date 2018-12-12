package com.adrianrafo.gcp4s.vision

case class VisionError(message: String)

case class VisionVertex(x: Int, y: Int)

case class VisionCoordinates(
    bottomLeft: VisionVertex,
    topLeft: VisionVertex,
    topRight: VisionVertex,
    bottomRight: VisionVertex)

case class VisionLabel(label: String, percent: Int)

case class VisionText(
    text: String,
    locale: String,
    confidence: Int,
    boundingPoly: VisionCoordinates)

case class VisionParagraph(text: String, confidence: Int, locale: List[String], words: List[VisionText])

case class VisionBlock(text: String, confidence: Int, locale: List[String], paragraphs: List[VisionParagraph])

case class VisionPage(text: String, confidence: Int, locale: List[String], blocks: List[VisionBlock])

case class VisionDocument(text: String, confidence: Int, locale: List[String], pages: List[VisionPage])

object Safety extends Enumeration {
  type Safety = Value
  val Unknown, VeryUnlikely, Unlikely, Possible, Likely, VeryLikely = Value
}

case class VisionSafeSearch(
    adult: Safety.Value,
    spoof: Safety.Value,
    medical: Safety.Value,
    violence: Safety.Value,
    racy: Safety.Value)

case class VisionCropHints(
    boundingPoly: VisionCoordinates,
    confidence: Int,
    importanceFraction: Double)

case class VisionObject()

case class VisionFace()

case class VisionLogo()

case class VisionLandMark()

case class VisionImageProperties()

case class VisionWebEntity()
