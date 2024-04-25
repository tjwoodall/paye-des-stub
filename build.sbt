import uk.gov.hmrc.DefaultBuildSettings.itSettings

lazy val appName = "paye-des-stub"

ThisBuild / scalaVersion := "2.13.13"
ThisBuild / majorVersion := 0

lazy val microservice = Project(appName, file("."))
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .disablePlugins(JUnitXmlReportPlugin)
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(CodeCoverageSettings.settings)
  .settings(
    routesImport += "controllers.Binders._",
    PlayKeys.playDefaultPort := 9689,
    libraryDependencies ++= AppDependencies()
  )
scalacOptions ++= Seq(
  "-Wconf:src=routes/.*:s",
  "-Wconf:cat=unused-imports&src=views/.*:s"
)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())
  .settings(libraryDependencies ++= AppDependencies.itDependencies)

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle it/Test/scalastyle")
