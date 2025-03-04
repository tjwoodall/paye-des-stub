/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package helpers

import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, GivenWhenThen}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.StandaloneWSRequest
import uk.gov.hmrc.mongo.test.MongoSupport

import java.util.concurrent.TimeUnit
import scala.concurrent.Await.result
import scala.concurrent.duration.{Duration, FiniteDuration}

trait BaseSpec
    extends AnyFeatureSpecLike
    with MongoSupport
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Matchers
    with GuiceOneServerPerSuite
    with GivenWhenThen
    with HttpClient {

  implicit override lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      "metrics.enabled"        -> false,
      "auditing.enabled"       -> false,
      "auditing.traceRequests" -> false,
      "mongodb.uri"            -> mongoUri,
      "run.mode"               -> "It"
    )
    .build()

  val timeoutInSeconds        = 5
  val timeout: FiniteDuration = Duration(timeoutInSeconds, TimeUnit.SECONDS)
  val serviceUrl              = s"http://localhost:$port"

  def getEndpoint(endpoint: String): StandaloneWSRequest#Response =
    result(
      awaitable = get(s"$serviceUrl/$endpoint"),
      atMost = timeout
    )

  def postEndpoint(endpoint: String, payload: String): StandaloneWSRequest#Response =
    result(
      awaitable = post(
        s"$serviceUrl/$endpoint",
        payload,
        Seq((HeaderNames.CONTENT_TYPE, "application/json"), (HeaderNames.ACCEPT, "application/vnd.hmrc.1.0+json"))
      ),
      atMost = timeout
    )
}
