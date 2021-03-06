package com.adrianrafo.gcp4s

import java.io.IOException

import cats.effect.IO
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

class ErrorHandlerServiceTest extends AnyFreeSpec with Matchers {

  def futureEx(n: Int): Int = {
    Thread.sleep(500)
    if (n == 0) throw new IOException else n
  }

  "ErrorHandlerService" - {
    "handleError" - {
      "should handle exceptions asynchronously" in {
        ErrorHandlerService
          .handleError[IO, Throwable, Int](futureEx(0), identity)
          .value
          .unsafeRunSync()
          .isLeft shouldBe true
      }

      "should return the right value asynchronously" in {
        ErrorHandlerService
          .handleError[IO, Throwable, Int](futureEx(1), identity)
          .value
          .unsafeRunSync
          .isRight shouldBe true
      }
    }
  }

}
