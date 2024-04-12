import sbt.*

object AppDependencies {
  private val bootstrapPlayVersion = "8.5.0"
  private val hmrcMongoPlayVersion = "1.8.0"

  private val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoPlayVersion,
    "uk.gov.hmrc"       %% "domain-play-30"            % "9.0.0"
  )

  private val test: Seq[ModuleID]   = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30"  % bootstrapPlayVersion,
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.31"
  ).map(_ % Test)

  val itDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoPlayVersion
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
