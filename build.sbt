import ProjectPlugin._

/////////////////////////
////     Modules     ////
/////////////////////////

lazy val common = project in file("modules/common") settings commonSettings

lazy val vision = project in file("modules/vision") settings visionSettings configs IntegrationTest dependsOn common

/////////////////////////
////      gcp4s      ////
/////////////////////////

lazy val allModules: Seq[ProjectReference] = Seq(
  common,
  vision
)

lazy val allModulesDeps: Seq[ClasspathDependency] =
  allModules.map(ClasspathDependency(_, None))

lazy val gcp4s = project
  .in(file("."))
  .aggregate(allModules: _*)
  .dependsOn(allModulesDeps: _*)
