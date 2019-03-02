package com.adrianrafo.gcp4s.vision

import cats.effect.IO
import org.scalatest._

import scala.concurrent.ExecutionContext.Implicits.global

class VisionAPITest extends FunSuite with Matchers with BeforeAndAfterAll {

  val (client, shutdown) = VisionClient.createClient[IO](None).allocated.unsafeRunSync()

  override protected def afterAll(): Unit =
    client.shutdown().unsafeRunSync()

  test("VisionService should recognize labels from an image") {
    val path     = "./modules/vision/src/it/resources/hand.jpg"
    val response = client.labelImage(None, Left(path)).unsafeRunSync().head
    response shouldBe 'right
    response.map(_.labels.map(_.label).contains("Hand")) shouldBe Right(true)
  }

  test("VisionService should recognize labels from an image with a response limit") {
    val path     = "./modules/vision/src/it/resources/hand.jpg"
    val response = client.labelImage(Some(3), Left(path)).unsafeRunSync().head
    response shouldBe 'right
    response.map(_.labels.size) shouldBe Right(3)
    response.map(_.labels.map(_.label).contains("Hand")) shouldBe Right(true)
  }

  test("VisionService should recognize text from an image") {
    val path     = "./modules/vision/src/it/resources/text.png"
    val response = client.textDetection(None, Left(path)).unsafeRunSync().head
    response shouldBe 'right
    response.map(_.texts.map(_.text).contains("lambda")) shouldBe Right(true)
  }

  test("VisionService should recognize document text from an image") {
    val path     = "./modules/vision/src/it/resources/document.png"
    val response = client.documentTextDetection(None, Left(path)).unsafeRunSync().head
    response shouldBe 'right
    response.map(_.text.contains("lambda")) shouldBe Right(true)
  }

  test("VisionService should recognize happy faces from an image") {
    val path     = "./modules/vision/src/it/resources/happy.jpg"
    val response = client.faceDetection(None, Left(path)).unsafeRunSync().head
    response shouldBe 'right
    response.map(_.faces.forall(_.joy == Grade.VeryLikely)) shouldBe Right(true)
  }

  test("VisionService should recognize anger faces from an image") {
    val path     = "./modules/vision/src/it/resources/anger.jpg"
    val response = client.faceDetection(None, Left(path)).unsafeRunSync().head
    response shouldBe 'right
    response.map(_.faces.forall(_.anger == Grade.VeryLikely)) shouldBe Right(true)
  }

  test("VisionService should recognize surprise faces from an image") {
    val path     = "./modules/vision/src/it/resources/surprise.jpg"
    val response = client.faceDetection(None, Left(path)).unsafeRunSync().head
    response shouldBe 'right
    response.map(_.faces.forall(_.surprise == Grade.VeryLikely)) shouldBe Right(true)
  }

  test("VisionService should recognize logos from an image") {
    val paths = List(
      "./modules/vision/src/it/resources/logos/logo1.png",
      "./modules/vision/src/it/resources/logos/logo2.jpeg"
    ).map(Left(_))
    val response = client.logoDetection(None, paths: _*).unsafeRunSync()
    response
      .map(_.map(_.logos.map(_.description.toLowerCase).mkString))
      .forall(res => res.exists(_.contains("google")) || res.exists(_.contains("scala"))) shouldBe true
  }

  test("VisionService should returns an errors and responses when has an invalid path") {
    val paths = List(
      "./modules/vision/src/it/resources/logos/logo1.png",
      "./modules/vision/src/it/resources/logos/logo.png"
    ).map(Left(_))
    val response = client.logoDetection(None, paths: _*).unsafeRunSync()
    response.exists(_.isRight) && response.exists(_.isLeft) shouldBe true
  }

  test("VisionService should recognize crop hints from an image") {
    val path     = "./modules/vision/src/it/resources/crop.jpeg"
    val response = client.cropHints(None, Left(path)).unsafeRunSync().head
    response shouldBe 'right
  }

  test("VisionService should recognize landmarks from an image") {
    val path     = "./modules/vision/src/it/resources/landmark.jpeg"
    val response = client.landmarkDetection(None, Left(path)).unsafeRunSync().head
    response shouldBe 'right
    response.map(_.landmarks.map(_.description).contains("Space Needle")) shouldBe Right(true)
  }

  test("VisionService should recognize properties from an image") {
    val path     = "./modules/vision/src/it/resources/hand.jpg"
    val response = client.imagePropertiesDetection(Left(path)).unsafeRunSync().head
    response shouldBe 'right
  }

  test("VisionService should recognize search safety from an image") {
    val path     = "./modules/vision/src/it/resources/web.png"
    val response = client.safeSearchDetection(Left(path)).unsafeRunSync().head
    response shouldBe 'right
    response.map(_.adult == Grade.VeryUnlikely) shouldBe Right(true)
    response.map(_.medical == Grade.VeryUnlikely) shouldBe Right(true)
    response.map(_.racy == Grade.VeryUnlikely) shouldBe Right(true)
    response.map(_.spoof == Grade.VeryUnlikely) shouldBe Right(true)
    response.map(_.violence == Grade.VeryUnlikely) shouldBe Right(true)
  }

  test("VisionService should recognize web entities from an image") {
    val path     = "./modules/vision/src/it/resources/web.png"
    val response = client.webEntitiesDetection(false, Some(3), Left(path)).unsafeRunSync().head
    response shouldBe 'right
    response.map(_.webLabels.map(_.label).contains("screenshot")) shouldBe Right(true)

  }

  test("VisionService should recognize objects from an image") {
    val path     = "./modules/vision/src/it/resources/objects.jpeg"
    val response = client.objectDetection(None, Left(path)).unsafeRunSync().head
    response shouldBe 'right
    response.map(_.objects.map(_.name).contains("Computer keyboard")) shouldBe Right(true)
    response.map(_.objects.map(_.name).contains("Mouse")) shouldBe Right(true)
  }

}
