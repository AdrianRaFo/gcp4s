package com.adrianrafo.gcp4s.vision

import cats.effect.IO
import com.google.cloud.vision.v1.ImageAnnotatorClient
import org.scalatest._

import scala.concurrent.ExecutionContext.Implicits.global

class VisionAPITest extends FunSuite with Matchers with BeforeAndAfterAll {

  val service: VisionAPI[IO]             = VisionAPI[IO]
  val clientIO: IO[ImageAnnotatorClient] = service.createClient(None)

  override protected def afterAll(): Unit =
    clientIO.map(_.shutdownNow()).unsafeRunSync()

  test("VisionService should recognize labels from an image") {
    val path = "./modules/vision/src/it/resources/hand.jpg"
    val response = clientIO
      .flatMap(service.labelImage(_, None, None, Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
    response.map(_.labels.map(_.label).contains("Hand")) shouldBe Right(true)
  }

  test("VisionService should recognize labels from an image with a response limit") {
    val path = "./modules/vision/src/it/resources/hand.jpg"
    val response = clientIO
      .flatMap(service.labelImage(_, None, Some(3), Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
    response.map(_.labels.size) shouldBe Right(3)
    response.map(_.labels.map(_.label).contains("Hand")) shouldBe Right(true)
  }

  test("VisionService should recognize text from an image") {
    val path = "./modules/vision/src/it/resources/text.png"
    val response = clientIO
      .flatMap(service.textDetection(_, None, Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
    response.map(_.texts.map(_.text).contains("lambda")) shouldBe Right(true)
  }

  test("VisionService should recognize document text from an image") {
    val path = "./modules/vision/src/it/resources/document.png"
    val response = clientIO
      .flatMap(service.documentTextDetection(_, None, Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
    response.map(_.text.contains("lambda")) shouldBe Right(true)
  }

  test("VisionService should recognize happy faces from an image") {
    val path = "./modules/vision/src/it/resources/happy.jpg"
    val response = clientIO
      .flatMap(service.faceDetection(_, None, None, Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
    response.map(_.faces.forall(_.joy == Grade.VeryLikely)) shouldBe Right(true)
  }

  test("VisionService should recognize anger faces from an image") {
    val path = "./modules/vision/src/it/resources/anger.jpg"
    val response = clientIO
      .flatMap(service.faceDetection(_, None, None, Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
    response.map(_.faces.forall(_.anger == Grade.VeryLikely)) shouldBe Right(true)
  }

  test("VisionService should recognize surprise faces from an image") {
    val path = "./modules/vision/src/it/resources/surprise.jpg"
    val response = clientIO
      .flatMap(service.faceDetection(_, None, None, Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
    response.map(_.faces.forall(_.surprise == Grade.VeryLikely)) shouldBe Right(true)
  }

  test("VisionService should recognize logos from an image") {
    val paths = List(
      "./modules/vision/src/it/resources/logos/logo1.jpeg",
      "./modules/vision/src/it/resources/logos/logo2.jpeg"
    ).map(Left(_))
    val response = clientIO
      .flatMap(service.logoDetection(_, None, None, paths: _*))
      .unsafeRunSync()
    response
      .map(_.map(_.logos.map(_.description).mkString))
      .forall(res => res.contains("Google") || res.contains("Scala")) shouldBe true
  }

  test("VisionService should recognize crop hints from an image") {
    val path = "./modules/vision/src/it/resources/crop.jpeg"
    val response = clientIO
      .flatMap(service.cropHints(_, None, Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
  }

  test("VisionService should recognize landmarks from an image") {
    val path = "./modules/vision/src/it/resources/landmark.jpeg"
    val response = clientIO
      .flatMap(service.landmarkDetection(_, None, None, Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
    response.map(_.landmarks.map(_.description).contains("Space Needle")) shouldBe Right(true)
  }

  test("VisionService should recognize properties from an image") {
    val path = "./modules/vision/src/it/resources/hand.jpg"
    val response = clientIO
      .flatMap(service.imagePropertiesDetection(_, None, Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
  }

  test("VisionService should recognize search safety from an image") {
    val path = "./modules/vision/src/it/resources/web.png"
    val response = clientIO
      .flatMap(service.safeSearchDetection(_, None, Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
    response.map(_.adult == Grade.VeryUnlikely) shouldBe Right(true)
    response.map(_.medical == Grade.VeryUnlikely) shouldBe Right(true)
    response.map(_.racy == Grade.VeryUnlikely) shouldBe Right(true)
    response.map(_.spoof == Grade.VeryUnlikely) shouldBe Right(true)
    response.map(_.violence == Grade.VeryUnlikely) shouldBe Right(true)
  }

  test("VisionService should recognize web entities from an image") {
    val path = "./modules/vision/src/it/resources/web.png"
    val response = clientIO
      .flatMap(service.webEntitiesDetection(_, None, Some(3), Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
    response.map(_.webLabels.map(_.label).contains("screenshot")) shouldBe Right(true)

  }

  test("VisionService should recognize objects from an image") {
    val path = "./modules/vision/src/it/resources/objects.jpeg"
    val response = clientIO
      .flatMap(service.objectDetection(_, None, None, Left(path)))
      .unsafeRunSync()
      .head
    response shouldBe 'right
    response.map(_.objects.map(_.name).contains("Computer keyboard")) shouldBe Right(true)
    response.map(_.objects.map(_.name).contains("Mouse")) shouldBe Right(true)
  }

}
