import sbt._
import sbt.Keys._
import org.scalafmt.sbt.ScalafmtPlugin.autoImport._
object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    lazy val V = new {
      val cats          = "1.3.1"
      val catsEffects   = "1.0.0"
      val scalaTest     = "3.0.5"
      val gcpClient     = "1.46.0"
      val gcpClientBeta = "0.64.0-beta"
    }
  }

  import autoImport._

  lazy val noPublishSettings: Seq[Def.Setting[_]] = Seq(
    publish := ((): Unit),
    publishLocal := ((): Unit),
    publishArtifact := false
  )

  private lazy val gcpModuleSettings: Seq[Def.Setting[_]] =
    Seq(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-core"   % V.cats,
        "org.typelevel" %% "cats-effect" % V.catsEffects))

  lazy val commonSettings = noPublishSettings ++ gcpModuleSettings ++ Seq(
    libraryDependencies +=
      "org.scalatest" %% "scalatest" % V.scalaTest % Test)

  lazy val visionSettings = gcpModuleSettings ++ Defaults.itSettings ++ Seq(
    libraryDependencies ++= Seq(
      "com.google.cloud" % "google-cloud-vision" % V.gcpClient,
      "org.scalatest"    %% "scalatest"          % V.scalaTest % "test;it"))

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      name := "gcp4s",
      organization := "com.adrianrafo",
      organizationName := "AdrianRaFo",
      scalaVersion := "2.12.7",
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
}
