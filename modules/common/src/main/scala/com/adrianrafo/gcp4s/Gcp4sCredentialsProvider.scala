package com.adrianrafo.gcp4s
import java.io.FileInputStream

import cats.effect.Effect
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials

trait Gcp4sCredentialsProvider[F[_]] {
  val credentials: F[FixedCredentialsProvider]
}

object Gcp4sCredentialsProvider {

  def getCredentials[F[_]](
    credentialsPath: String
  )(implicit F: Effect[F]): F[Gcp4sCredentialsProvider[F]] =
    F.delay(new Gcp4sCredentialsProvider[F] {

      val credentials: F[FixedCredentialsProvider] =
        F.delay(
          FixedCredentialsProvider
            .create(ServiceAccountCredentials.fromStream(new FileInputStream(credentialsPath)))
        )
    })

}
