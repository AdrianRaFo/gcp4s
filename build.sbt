import ProjectPlugin._

/////////////////////////
////     Modules     ////
/////////////////////////

lazy val common = project in file("modules/common") settings (name := "gcp4s-common") settings commonSettings

lazy val vision = project in file("modules/vision") settings (name := "gcp4s-vision")  settings visionSettings configs IntegrationTest dependsOn common

/////////////////////////
////      gcp4s      ////
/////////////////////////

lazy val allModules: Seq[ProjectReference] = Seq(
  common,
  vision
)

lazy val allModulesDeps: Seq[ClasspathDependency] =
  allModules.map(ClasspathDependency(_, None))

lazy val root = project
  .in(file("."))
  .aggregate(allModules: _*)
  .dependsOn(allModulesDeps: _*)
