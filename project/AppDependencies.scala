import play.core.PlayVersion
import sbt._

object AppDependencies {

  private val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"        %% "bootstrap-backend-play-28"    % "5.7.0",
    "uk.gov.hmrc.mongo"  %% "hmrc-mongo-play-28"           % "0.51.0",
    "uk.gov.hmrc"        %% "domain"                       % "6.1.0-play-28",
    "uk.gov.hmrc"        %% "tax-year"                     % "1.4.0",
    "uk.gov.hmrc"        %% "hmrc-stubs-core"              % "6.2.0-play-26"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % "0.51.0",
    "org.scalatest"          %% "scalatest"                % "3.2.9",
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0",
    "org.scalatestplus"      %% "mockito-3-4"              % "3.2.9.0",
    "com.vladsch.flexmark"   % "flexmark-all"              % "0.35.10",
    "org.mockito"            % "mockito-core"              % "2.10.0",
    "org.pegdown"            % "pegdown"                   % "1.6.0",
    "com.typesafe.play"      %% "play-test"                % PlayVersion.current,
    "org.scalaj"             %% "scalaj-http"              % "2.4.2"
  ).map(_ % "test, it")

  private val silencerDependencies: Seq[ModuleID] = Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.1" cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % "1.7.1" % Provided cross CrossVersion.full
  )

  def apply(): Seq[ModuleID] = compile ++ test ++ silencerDependencies
}
