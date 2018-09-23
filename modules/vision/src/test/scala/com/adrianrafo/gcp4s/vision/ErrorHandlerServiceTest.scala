package com.adrianrafo.gcp4s.vision

import java.io.IOException

import cats.effect.IO
import org.scalatest._

class ErrorHandlerServiceTest extends FreeSpec with Matchers {
  val service = new ErrorHandlerService[IO]

  def ex(n: Int): Int = if (n == 0) throw new IOException else n

  "ErrorHandlerService" - {
    "handleError" - {
      "should handle exceptions" in {
        service.handleError[Int](ex(0)).unsafeRunSync() shouldBe 'left
      }

      "should return right value" in {
        service.handleError[Int](ex(1)).unsafeRunSync() shouldBe 'right
      }
    }
  }

}
