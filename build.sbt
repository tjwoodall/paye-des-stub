import _root_.play.sbt.routes.RoutesKeys.routesImport
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val appName = "paye-des-stub"
val silencerVersion = "1.7.1"

def unitFilter(name: String): Boolean = name startsWith "unit"
def itTestFilter(name: String): Boolean = name startsWith "it"

lazy val microservice = (project in file("."))
  .settings(defaultSettings())
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .settings(Test / testOptions := Seq(Tests.Filter(unitFilter), Tests.Argument(TestFrameworks.ScalaTest, "-eT")))
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    name := appName,
    scalaSettings,
    majorVersion := 0,
    scalaVersion := "2.12.12",
    publishingSettings,
    retrieveManaged := true,
    routesImport += "uk.gov.hmrc.payedesstub.controllers.Binders._",
    resolvers += Resolver.jcenterRepo,
    PlayKeys.playDefaultPort := 9689,
    Test / javaOptions += "-Dconfig.resource=test.application.conf",
    Test / fork := false,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testOptions := Seq(Tests.Filter(itTestFilter), Tests.Argument(TestFrameworks.ScalaTest, "-eT")),
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory) (base => Seq(base / "test")).value,
    libraryDependencies ++= AppDependencies(),
    coverageMinimumStmtTotal := 80,
    coverageFailOnMinimum := true,
    coverageExcludedPackages :=
      "<empty>;com.kenshoo.play.metrics.*;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*;live.*;uk.gov.hmrc.BuildInfo;uk.gov.hmrc.payedesstub.config",
    addTestReportOption(IntegrationTest, "int-test-reports")
  )
scalacOptions ++= Seq(
  "-P:silencer:pathFilters=views;routes"
)
