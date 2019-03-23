package com.adrianrafo.gcp4s.vision

import cats.Show
import cats.derived.semi
import cats.instances.all._

object shows {
  implicit val VisionErrorShow: Show[VisionError]                 = semi.show[VisionError]
  implicit val VisionVertexShow: Show[VisionVertex]               = semi.show[VisionVertex]
  implicit val VisionPositionShow: Show[VisionPosition]           = semi.show[VisionPosition]
  implicit val VisionCoordinatesShow: Show[VisionCoordinates]     = semi.show[VisionCoordinates]
  implicit val VisionLocationShow: Show[VisionLocation]           = semi.show[VisionLocation]
  implicit val GradeShow: Show[Grade.Value]                       = Show.fromToString[Grade.Value]
  implicit val visionLabelShow: Show[VisionLabel]                 = semi.show[VisionLabel]
  implicit val visionLabelResponseShow: Show[VisionLabelResponse] = semi.show[VisionLabelResponse]
  implicit val visionTextShow: Show[VisionText]                   = semi.show[VisionText]
  implicit val visionTextResponseShow: Show[VisionTextResponse]   = semi.show[VisionTextResponse]
  implicit val visionLanguageShow: Show[VisionLanguage]           = semi.show[VisionLanguage]
  implicit val visionWordShow: Show[VisionWord]                   = semi.show[VisionWord]
  implicit val visionParagraphShow: Show[VisionParagraph]         = semi.show[VisionParagraph]
  implicit val visionBlockShow: Show[VisionBlock]                 = semi.show[VisionBlock]
  implicit val visionPageShow: Show[VisionPage]                   = semi.show[VisionPage]
  implicit val visionDocumentShow: Show[VisionDocument]           = semi.show[VisionDocument]
  implicit val visionObjectShow: Show[VisionObject]               = semi.show[VisionObject]
  implicit val vorShow: Show[VisionObjectResponse]                = semi.show[VisionObjectResponse]
  implicit val visionFaceShow: Show[VisionFace]                   = semi.show[VisionFace]
  implicit val visionFaceResponseShow: Show[VisionFaceResponse]   = semi.show[VisionFaceResponse]
  implicit val visionLogoShow: Show[VisionLogo]                   = semi.show[VisionLogo]
  implicit val visionLogoResponseShow: Show[VisionLogoResponse]   = semi.show[VisionLogoResponse]
  implicit val visionLandMarkShow: Show[VisionLandMark]           = semi.show[VisionLandMark]
  implicit val vlmrShow: Show[VisionLandMarkResponse]             = semi.show[VisionLandMarkResponse]
  implicit val visionSafeSearchShow: Show[VisionSafeSearch]       = semi.show[VisionSafeSearch]
  implicit val MatchLevelShow: Show[MatchLevel.Value]             = Show.fromToString[MatchLevel.Value]
  implicit val visionWebImageMatchShow: Show[VisionWebImageMatch] = semi.show[VisionWebImageMatch]
  implicit val visionWebPageMatchShow: Show[VisionWebPageMatch]   = semi.show[VisionWebPageMatch]
  implicit val visionWebEntityShow: Show[VisionWebEntity]         = semi.show[VisionWebEntity]
  implicit val visionWebLabelShow: Show[VisionWebLabel]           = semi.show[VisionWebLabel]
  implicit val visionWebDetectionShow: Show[VisionWebDetection]   = semi.show[VisionWebDetection]
  implicit val visionCropHintShow: Show[VisionCropHint]           = semi.show[VisionCropHint]
  implicit val vchrShow: Show[VisionCropHintResponse]             = semi.show[VisionCropHintResponse]
  implicit val visionColorShow: Show[VisionColor]                 = semi.show[VisionColor]
  implicit val vipShow: Show[VisionImageProperties]               = semi.show[VisionImageProperties]
}
