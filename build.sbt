import _root_.play.sbt.routes.RoutesKeys.routesImport
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val appName = "paye-des-stub"

def unitFilter(name: String): Boolean   = name startsWith "unit"
def itTestFilter(name: String): Boolean = name startsWith "it"

lazy val microservice = Project(appName, file("."))
  .settings(defaultSettings())
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .settings(Test / testOptions := Seq(Tests.Filter(unitFilter)))
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    scalaSettings,
    majorVersion := 0,
    scalaVersion := "2.12.16",
    publishingSettings,
    retrieveManaged := true,
    routesImport += "controllers.Binders._",
    resolvers += Resolver.jcenterRepo,
    PlayKeys.playDefaultPort := 9689,
    Test / javaOptions += "-Dconfig.resource=test.application.conf",
    Test / fork := false,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testOptions := Seq(Tests.Filter(itTestFilter)),
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "test")).value,
    libraryDependencies ++= AppDependencies(),
    coverageMinimumStmtTotal := 100,
    coverageFailOnMinimum := true,
    coverageExcludedPackages :=
      "<empty>;com.kenshoo.play.metrics.*;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*;live.*;uk.gov.hmrc.BuildInfo;config",
    addTestReportOption(IntegrationTest, "int-test-reports")
  )
scalacOptions ++= Seq(
  "-Wconf:src=routes/.*:s",
  "-Wconf:cat=unused-imports&src=views/.*:s"
)

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle")
