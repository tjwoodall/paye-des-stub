import sbt.Setting
import scoverage.ScoverageKeys.*

object CodeCoverageSettings {
  private val excludedPackages: Seq[String] = Seq(
    "<empty>",
    ".*definition.*",
    "prod.*",
    "testOnlyDoNotUseInAppConf.*",
    "live.*",
    "uk.gov.hmrc.BuildInfo",
    "config"
  )

  val settings: Seq[Setting[?]] = Seq(
    coverageExcludedPackages := excludedPackages.mkString(";"),
    coverageMinimumStmtTotal := 100,
    coverageFailOnMinimum := true,
    coverageHighlighting := true
  )

}
