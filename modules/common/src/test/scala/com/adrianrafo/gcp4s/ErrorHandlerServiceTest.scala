package com.adrianrafo.gcp4s

import java.io.IOException

import cats.effect.IO
import org.scalatest._

import scala.concurrent.ExecutionContext.Implicits.global

class ErrorHandlerServiceTest extends FreeSpec with Matchers {

  def futureEx(n: Int) = {
    Thread.sleep(500)
    if (n == 0) throw new IOException else n
  }

  def ex(n: Int): Int = if (n == 0) throw new IOException else n

  "ErrorHandlerService" - {
    "handleError" - {
      "should handle exceptions" in {
        ErrorHandlerService
          .handleError[IO, Throwable, Int](ex(0), identity)
          .value
          .unsafeRunSync() shouldBe 'left
      }

      "should return right value" in {
        ErrorHandlerService
          .handleError[IO, Throwable, Int](ex(1), identity)
          .value
          .unsafeRunSync shouldBe 'right
      }
    }
    "asyncHandleError" - {
      "should handle exceptions asynchronously" in {
        ErrorHandlerService
          .asyncHandleError[IO, Throwable, Int](futureEx(0), identity)
          .value
          .unsafeRunSync() shouldBe 'left
      }

      "should return right value asynchronously" in {
        ErrorHandlerService
          .asyncHandleError[IO, Throwable, Int](futureEx(1), identity)
          .value
          .unsafeRunSync shouldBe 'right
      }
    }
  }

}
