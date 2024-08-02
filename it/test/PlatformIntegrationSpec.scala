/*
 * Copyright 2024 HM Revenue & Customs
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

import org.apache.pekko.stream.Materializer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._

/** Testcase to verify the capability of integration with the API platform.
  *
  * 1a, To expose API's to Third Party Developers, the service needs to make the API definition available under
  * api/definition GET endpoint 1b, The endpoints need to be defined in an application.yaml file for all versions For
  * all of the endpoints defined documentation will be provided and be available under
  * api/documentation/[version]/[endpoint name] GET endpoint Example: api/documentation/1.0/Fetch-Some-Data
  *
  * See: https://confluence.tools.tax.service.gov.uk/display/ApiPlatform/API+Platform+Architecture+with+Flows
  */
class PlatformIntegrationSpec extends AnyWordSpecLike with Matchers with GuiceOneAppPerTest {
  implicit def mat: Materializer = app.injector.instanceOf[Materializer]

  "paye-des-stub" should {

    "provide definition endpoint" in {
      val description: String = "Lets you set up test data for the Individual PAYE APIs: " +
        "Individual PAYE, Individual Benefits, Individual Employment, Individual Income and Individual Tax."

      val apiDefinitionJson: JsValue = Json.parse(
        s"""
          |{
          |    "scopes": [],
          |    "api": {
          |        "name": "Individual PAYE Test Support",
          |        "description": "$description",
          |        "context": "individual-paye-test-support",
          |        "isTestSupport": true,
          |        "versions": [
          |            {
          |                "version": "1.0",
          |                "status": "BETA",
          |                "endpointsEnabled": true
          |            }
          |        ]
          |    }
          |}
        """.stripMargin
      )

      val result = route(app, FakeRequest(GET, "/api/definition")).get
      status(result)        shouldBe OK
      contentAsJson(result) shouldBe apiDefinitionJson
    }

    "provide yaml documentation" in {
      val result = route(app, FakeRequest(GET, "/api/conf/1.0/application.yaml")).get
      status(result)        shouldBe OK
      contentAsString(result) should include("""openapi: 3.0.3
                                                |info:
                                                |  title: Individual PAYE Test Support""".stripMargin)
    }
  }
}
