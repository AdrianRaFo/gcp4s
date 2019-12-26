package com.adrianrafo.gcp4s.vision

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GradeSpec extends AnyFlatSpec with Matchers {

  val fakePosition: VisionPosition = VisionPosition(List.empty)

  "toGrade" should "return the right Grade when a valid value is passed" in {
    Grade.fromValue(1) shouldBe Grade.VeryUnlikely
  }
  "toGrade" should "return unknown when an invalid value is passed" in {
    Grade.fromValue(-1) shouldBe Grade.Unknown
  }

  "VisionFace" should "return the expected emotion" in {
    val joy = VisionFace(Grade.Likely, Grade.Unlikely, Grade.Unlikely, Grade.Unlikely, fakePosition)
    joy.isMainlyJoy shouldBe true
    joy.isMainlySurprised shouldBe false
    joy.isMainlyAnger shouldBe false
    joy.isExpressionless shouldBe false

    val surprise = VisionFace(Grade.Unlikely, Grade.Likely, Grade.Unlikely, Grade.Unlikely, fakePosition)
    surprise.isMainlyJoy shouldBe false
    surprise.isMainlySurprised shouldBe true
    surprise.isMainlyAnger shouldBe false
    surprise.isExpressionless shouldBe false

    val anger = VisionFace(Grade.Unlikely, Grade.Unlikely, Grade.Likely, Grade.Unlikely, fakePosition)
    anger.isMainlyJoy shouldBe false
    anger.isMainlySurprised shouldBe false
    anger.isMainlyAnger shouldBe true
    anger.isExpressionless shouldBe false

    val sorrow = VisionFace(Grade.Unlikely, Grade.Unlikely, Grade.Unlikely, Grade.Likely, fakePosition)
    sorrow.isMainlyJoy shouldBe false
    sorrow.isMainlySurprised shouldBe false
    sorrow.isMainlyAnger shouldBe false
    sorrow.isMainlySorrowed shouldBe true
    sorrow.isExpressionless shouldBe false

    val joyAndSurprise = VisionFace(Grade.VeryLikely, Grade.VeryLikely, Grade.Likely, Grade.Likely, fakePosition)
    joyAndSurprise.isMainlyJoy shouldBe true
    joyAndSurprise.isMainlySurprised shouldBe true
    joyAndSurprise.isMainlyAnger shouldBe false
    joyAndSurprise.isMainlySorrowed shouldBe false
    joyAndSurprise.isExpressionless shouldBe false

    val surpriseAndAnger = VisionFace(Grade.Likely, Grade.VeryLikely, Grade.VeryLikely, Grade.VeryLikely, fakePosition)
    surpriseAndAnger.isMainlyJoy shouldBe false
    surpriseAndAnger.isMainlySurprised shouldBe true
    surpriseAndAnger.isMainlyAnger shouldBe true
    surpriseAndAnger.isMainlySorrowed shouldBe true
    surpriseAndAnger.isExpressionless shouldBe false

    VisionFace(Grade.Likely, Grade.Likely, Grade.Likely, Grade.Likely, fakePosition).isExpressionless shouldBe true
  }

}
