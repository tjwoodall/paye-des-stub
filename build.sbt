import uk.gov.hmrc.DefaultBuildSettings.itSettings

lazy val appName = "paye-des-stub"

ThisBuild / scalaVersion := "3.4.2"
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
scalacOptions := scalacOptions.value.diff(Seq("-Wunused:all"))

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
