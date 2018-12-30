package com.adrianrafo.gcp4s.vision

import org.scalatest._

class GradeSpec extends FlatSpec with Matchers {

  "toGrade" should "return the right Grade when a valid value is passed" in {
    Grade.fromValue(1) shouldBe Grade.VeryUnlikely
  }
  "toGrade" should "return unknown when an invalid value is passed" in {
    Grade.fromValue(-1) shouldBe Grade.Unknown
  }

}
