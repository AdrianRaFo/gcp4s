package com.adrianrafo.gcp4s.vision

import cats.effect.IO
import com.google.cloud.vision.v1.Feature.Type
import org.scalatest._

import scala.concurrent.ExecutionContext.Implicits.global

class VisionAPITest extends FunSuite with Matchers {

  val service = VisionAPI[IO]

  test("VisionService should get labels for an image") {
    val path = "./modules/vision/src/it/resources/hand.jpg"
    val response = service
      .createClient(None)
      .flatMap(service.labelImage(_, Type.LABEL_DETECTION, Left(path), None))
      .unsafeRunSync()
    println(response)
    response shouldBe 'right
  }

}
