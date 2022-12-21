import play.core.PlayVersion
import sbt._

object AppDependencies {
  private val bootstrapPlayVersion = "7.12.0"
  private val hmrcMongoPlayVersion = "0.74.0"

  private val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"        % hmrcMongoPlayVersion,
    "uk.gov.hmrc"       %% "domain"                    % "8.1.0-play-28"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-28" % hmrcMongoPlayVersion,
    "org.scalatest"       %% "scalatest"               % "3.2.14",
    "uk.gov.hmrc"         %% "bootstrap-test-play-28"  % bootstrapPlayVersion,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.62.2",
    "org.mockito"         %% "mockito-scala-scalatest" % "1.17.12",
    "com.typesafe.play"   %% "play-test"               % PlayVersion.current,
    "org.scalaj"          %% "scalaj-http"             % "2.4.2"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID]      = compile ++ test
}
