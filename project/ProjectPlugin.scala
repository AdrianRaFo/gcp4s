import org.scalafmt.sbt.ScalafmtPlugin.autoImport._
import sbt.Keys._
import sbt._

object ProjectPlugin extends AutoPlugin {

  lazy val commonSettings: Seq[Def.Setting[_]] = gcpModuleSettings ++ Seq(
    libraryDependencies ++=
      Seq("com.google.api" % "gax" % "1.40.0", "org.scalatest" %% "scalatest" % V.scalaTest % Test))
  lazy val visionSettings: Seq[Def.Setting[_]] = gcpModuleSettings ++ Defaults.itSettings ++ Seq(
    libraryDependencies ++= Seq(
      "com.google.cloud" % "google-cloud-vision" % V.gcpClient,
      "org.scalatest"    %% "scalatest"          % V.scalaTest % "test;it"))

  import autoImport._
  private lazy val gcpModuleSettings: Seq[Def.Setting[_]] =
    Seq(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-core"   % V.cats,
        "org.typelevel" %% "cats-effect" % V.catsEffects))

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      name := "gcp4s",
      organization := "com.adrianrafo",
      organizationName := "AdrianRaFo",
      scalaVersion := "2.12.8",
      scalacOptions := Seq(
        "-deprecation",
        "-encoding",
        "UTF-8",
        "-feature",
        "-language:existentials",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-unchecked",
        "-Xlint",
        "-Ypartial-unification",
        "-Yno-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen",
        "-Ywarn-value-discard",
        "-Xfuture",
        "-Ywarn-unused-import"
      ),
      scalafmtCheck := true,
      scalafmtOnCompile := true
    )

  object autoImport {

    lazy val V = new {
      val cats        = "1.6.0"
      val catsEffects = "1.2.0"
      val scalaTest   = "3.0.6"
      val gcpClient   = "1.63.0"
    }

  }
}
