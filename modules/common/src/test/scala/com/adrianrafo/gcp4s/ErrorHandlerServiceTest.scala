package com.adrianrafo.gcp4s

import java.io.IOException

import cats.effect.IO
import org.scalatest._

class ErrorHandlerServiceTest extends FreeSpec with Matchers {

  def ex(n: Int): Int = if (n == 0) throw new IOException else n

  "ErrorHandlerService" - {
    "handleError" - {
      "should handle exceptions" in {
        ErrorHandlerService
          .handleError[IO, Throwable, Int](ex(0), identity)
          .unsafeRunSync() shouldBe 'left
      }

      "should return right value" in {
        ErrorHandlerService
          .handleError[IO, Throwable, Int](ex(1), identity)
          .unsafeRunSync() shouldBe 'right
      }
    }
  }

}
