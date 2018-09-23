lazy val vision = project in file("modules/vision") settings (libraryDependencies ++= Seq(
  "org.typelevel"    %% "cats-core"          % "1.3.1",
  "org.typelevel"    %% "cats-effect"        % "1.0.0",
  "com.google.cloud" % "google-cloud-vision" % "1.45.0",
  "org.scalatest"    %% "scalatest"          % "3.0.5" % Test
))

lazy val allModules: Seq[ProjectReference] = Seq(
  vision
)

lazy val allModulesDeps: Seq[ClasspathDependency] =
  allModules.map(ClasspathDependency(_, None))

lazy val gcp4s = project
  .in(file("."))
  .settings(name := "gcp4s", organization := "com.adrianrafo")
  .settings(scalaVersion := "2.12.6", scalacOptions += "-Ypartial-unification")
  .aggregate(allModules: _*)
  .dependsOn(allModulesDeps: _*)
