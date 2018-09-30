lazy val vision = project in file("modules/vision") settings (libraryDependencies ++= Seq(
  "org.typelevel"    %% "cats-core"          % "1.3.1",
  "org.typelevel"    %% "cats-effect"        % "1.0.0",
  "com.google.cloud" % "google-cloud-vision" % "1.45.0",
  "org.scalatest"    %% "scalatest"          % "3.0.5" % "test;it"
), Defaults.itSettings) configs IntegrationTest

lazy val allModules: Seq[ProjectReference] = Seq(
  vision
)

lazy val allModulesDeps: Seq[ClasspathDependency] =
  allModules.map(ClasspathDependency(_, None))

lazy val gcp4s = project
  .in(file("."))
  .settings(name := "gcp4s", organization := "com.adrianrafo")
  .settings(
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
    )
  )
  .aggregate(allModules: _*)
  .dependsOn(allModulesDeps: _*)
