package com.adrianrafo.gcp4s.vision

import cats.effect.IO
import org.scalatest._

class VisionAPITest extends FunSuite with Matchers {

  val service = VisionAPI[IO]

  test("VisionService should get labels for an image") {
    val path = "./modules/vision/src/it/resources/hand.jpg"
    val response =
      service.createClient(None).flatMap(service.labelImage(_, Left(path), None)).unsafeRunSync()
    println(response)
    response shouldBe 'right
  }

}
