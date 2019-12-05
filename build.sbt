import _root_.play.sbt.routes.RoutesKeys.routesImport
import play.core.PlayVersion
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val appName = "paye-des-stub"

lazy val appDependencies: Seq[ModuleID] = compile ++ test()

lazy val compile = Seq(
  "uk.gov.hmrc" %% "simple-reactivemongo" % "7.21.0-play-25",
  "uk.gov.hmrc" %% "bootstrap-play-25" % "5.1.0",
  "uk.gov.hmrc" %% "domain" % "5.6.0-play-25"
)

def test(scope: String = "test,it") = Seq(
  "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-25" % scope,
  "uk.gov.hmrc" %% "reactivemongo-test" % "4.15.0-play-25" % scope,
  "org.scalatest" %% "scalatest" % "3.0.1" % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % scope,
  "org.mockito" % "mockito-core" % "2.10.0" % scope,
  "org.pegdown" % "pegdown" % "1.6.0" % scope,
  "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
  "org.scalaj" %% "scalaj-http" % "2.3.0" % scope
)

lazy val plugins: Seq[Plugins] = Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
lazy val playSettings: Seq[Setting[_]] = Seq.empty

def unitFilter(name: String): Boolean = name startsWith "unit"
def itTestFilter(name: String): Boolean = name startsWith "it"

lazy val microservice = (project in file("."))
  .enablePlugins(plugins: _*)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    name := appName,
    targetJvm := "jvm-1.8",
    libraryDependencies ++= appDependencies,
    parallelExecution in Test := false,
    fork in Test := false,
    retrieveManaged := true,
    testOptions in Test := Seq(Tests.Filter(unitFilter), Tests.Argument(TestFrameworks.ScalaTest, "-eT")),
    majorVersion := 0
  )
  .settings(routesImport += "uk.gov.hmrc.payedesstub.controllers.Binders._")
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "resources")
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    testOptions in IntegrationTest := Seq(Tests.Filter(itTestFilter), Tests.Argument(TestFrameworks.ScalaTest, "-eT")),
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest) (base => Seq(base / "test")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false)
  .settings(
    resolvers += Resolver.jcenterRepo
  )

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
  tests map {
    test => Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
  }

// Coverage configuration
coverageMinimum := 80
coverageFailOnMinimum := true
coverageExcludedPackages :=
  "<empty>;com.kenshoo.play.metrics.*;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*;live.*;uk.gov.hmrc.BuildInfo;uk.gov.hmrc.payedesstub.config"
