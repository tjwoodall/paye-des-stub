import _root_.play.sbt.routes.RoutesKeys.routesImport
import play.core.PlayVersion
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val appName = "paye-des-stub"
val silencerVersion = "1.7.1"

lazy val appDependencies: Seq[ModuleID] = compile ++ test()

def unitFilter(name: String): Boolean = name startsWith "unit"
def itTestFilter(name: String): Boolean = name startsWith "it"

lazy val compile = Seq(
  "uk.gov.hmrc" %% "simple-reactivemongo" % "7.31.0-play-26",
  "uk.gov.hmrc" %% "bootstrap-backend-play-26" % "3.2.0",
  "uk.gov.hmrc" %% "domain" % "5.10.0-play-26",
  compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
  "com.github.ghik"        % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full

)

def test(scope: String = "test, it") = Seq(
  "uk.gov.hmrc" %% "hmrctest" % "3.10.0-play-26" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "4.22.0-play-26" % scope,
  "org.scalatest" %% "scalatest" % "3.0.9" % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % scope,
  "org.mockito" % "mockito-core" % "2.10.0" % scope,
  "org.pegdown" % "pegdown" % "1.6.0" % scope,
  "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
  "org.scalaj" %% "scalaj-http" % "2.3.0" % scope
)

defaultSettings()


lazy val microservice = (project in file("."))
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "resources")
  .settings(testOptions in Test := Seq(Tests.Filter(unitFilter), Tests.Argument(TestFrameworks.ScalaTest, "-eT")))
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)

enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)

name := appName
scalaSettings
majorVersion := 0
scalaVersion := "2.12.12"
publishingSettings
integrationTestSettings
retrieveManaged := true
routesImport += "uk.gov.hmrc.payedesstub.controllers.Binders._"
resolvers += Resolver.jcenterRepo
PlayKeys.playDefaultPort := 9689

javaOptions in Test += "-Dconfig.resource=test.application.conf"
fork in Test := true
testOptions in IntegrationTest := Seq(Tests.Filter(itTestFilter), Tests.Argument(TestFrameworks.ScalaTest, "-eT"))
unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest) (base => Seq(base / "test")).value

libraryDependencies ++= appDependencies

// Coverage configuration
coverageMinimum := 80
coverageFailOnMinimum := true
coverageExcludedPackages :=
  "<empty>;com.kenshoo.play.metrics.*;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*;live.*;uk.gov.hmrc.BuildInfo;uk.gov.hmrc.payedesstub.config"

scalacOptions ++= Seq(
  "-P:silencer:pathFilters=views;routes"
)
