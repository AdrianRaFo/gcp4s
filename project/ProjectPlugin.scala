import sbt.Keys._
import sbt._

object ProjectPlugin extends AutoPlugin {

  object autoImport {

    lazy val V = new {
      val cats       = "2.1.0"
      val catsEffect = "2.0.0"
      val scalaTest  = "3.1.0"
      val gax        = "1.50.1"
      val gcpClient  = "1.99.0"
      val kittens    = "2.0.0"
    }

  }

  import autoImport._

  private val gcpModuleSettings: Seq[Def.Setting[_]] =
    Seq(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-core"   % V.cats,
        "org.typelevel" %% "cats-effect" % V.catsEffect
      )
    )

  lazy val commonSettings: Seq[Def.Setting[_]] = gcpModuleSettings ++ Seq(
    libraryDependencies ++=
      Seq(
        "org.typelevel"  %% "kittens"   % V.kittens,
        "com.google.api" % "gax"        % V.gax,
        "org.scalatest"  %% "scalatest" % V.scalaTest % Test
      )
  )

  lazy val visionSettings: Seq[Def.Setting[_]] = gcpModuleSettings ++ Defaults.itSettings ++ Seq(
    libraryDependencies ++= Seq(
      "com.google.cloud" % "google-cloud-vision" % V.gcpClient,
      "org.scalatest"    %% "scalatest"          % V.scalaTest % "test;it"
    )
  )

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      name := "gcp4s",
      organization := "com.adrianrafo",
      organizationName := "AdrianRaFo",
      scalaVersion := "2.13.1",
      crossScalaVersions := Seq("2.12.10", "2.13.1")
    )

}
