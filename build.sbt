name := "gcp4s"

version := "0.1"

scalaVersion := "2.12.6"

scalacOptions += "-Ypartial-unification"

lazy val vision = project in file("modules/vision") settings (libraryDependencies ++= Seq(
  "org.typelevel"    %% "cats-core"          % "1.3.1",
  "com.google.cloud" % "google-cloud-vision" % "1.45.0"))

lazy val allModules: Seq[ProjectReference] = Seq(
  vision
)

lazy val allModulesDeps: Seq[ClasspathDependency] =
  allModules.map(ClasspathDependency(_, None))

lazy val gcp4s = project in file(".") settings (name := "Ane") aggregate (allModules: _*) dependsOn (allModulesDeps: _*)
