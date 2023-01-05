/*
 * Copyright 2023 HM Revenue & Customs
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

package it.helpers

import org.scalatest._
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import scalaj.http.{Http, HttpResponse}
import uk.gov.hmrc.mongo.test.MongoSupport

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration, FiniteDuration}

trait BaseSpec
    extends AnyFeatureSpecLike
    with MongoSupport
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Matchers
    with GuiceOneServerPerSuite
    with GivenWhenThen {

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

  def getEndpoint(endpoint: String): HttpResponse[String] =
    Http(s"$serviceUrl/$endpoint").asString

  def postEndpoint(endpoint: String, payload: String): HttpResponse[String] =
    Http(s"$serviceUrl/$endpoint")
      .method("POST")
      .header(HeaderNames.CONTENT_TYPE, "application/json")
      .header(HeaderNames.ACCEPT, "application/vnd.hmrc.1.0+json")
      .postData(payload)
      .asString
}
