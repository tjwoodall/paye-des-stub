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

import helpers.BaseSpec
import org.mongodb.scala.SingleObservableFuture
import play.api.http.Status.{CREATED, NOT_FOUND, OK}
import play.api.libs.ws.StandaloneWSRequest
import repositories.IndividualTaxRepository

import scala.concurrent.Await.result

class IndividualTaxSpec extends BaseSpec {
  Feature("Fetch individual tax summary data") {
    Scenario("No data is returned because utr and taxYear are not found") {
      When("I request tax summary data for a given utr and taxYear")
      val response = fetchIndividualTaxData("1111111111", "2016-17")

      Then("The response should indicate that no data was found")
      response.status shouldBe NOT_FOUND
    }

    Scenario("Tax summary data can be primed") {
      When("I request tax summary data for a given utr and taxYear")
      val response = primeIndividualTaxData("1111111111", "2016-17", """{ "scenario": "HAPPY_PATH_1" }""")

      Then("The response should indicate that the summary has been created")
      response.status shouldBe CREATED
    }

    Scenario(
      "Individual tax summary data is returned for the given utr and taxYear when primed with the default scenario"
    ) {
      When("I prime tax data for a given utr and taxYear")
      val primeResponse = primeIndividualTaxData("1111111111", "2016-17", "{}")

      Then("The response should contain individual tax summary data")
      primeResponse.status shouldBe CREATED

      And("I request tax summary data for a given utr and taxYear")
      val fetchResponse = fetchIndividualTaxData("1111111111", "2016")

      And("The response should contain individual tax summary data")
      fetchResponse.status shouldBe OK
    }

    Scenario(
      "Individual tax summary data is returned for the given utr and taxYear when primed with a specific scenario"
    ) {
      When("I prime tax summary data for a given utr, taxYear and test scenario")
      val primeResponse = primeIndividualTaxData("1111111111", "2016-17", """{"scenario":"HAPPY_PATH_1"}""")

      Then("The response should contain individual tax summary data")
      primeResponse.status shouldBe CREATED

      And("I request tax summary data for a given utr and taxYear")
      val fetchResponse = fetchIndividualTaxData("1111111111", "2016")

      And("The response should contain individual tax data")
      fetchResponse.status shouldBe OK
    }
  }

  private def primeIndividualTaxData(utr: String, taxYear: String, payload: String): StandaloneWSRequest#Response =
    postEndpoint(s"sa/$utr/tax/annual-summary/$taxYear", payload)

  private def fetchIndividualTaxData(utr: String, taxYear: String): StandaloneWSRequest#Response =
    getEndpoint(s"self-assessment-prepop/individual/$utr/tax-summary/tax-year/$taxYear")

  override protected def beforeEach(): Unit = {
    val repository = app.injector.instanceOf[IndividualTaxRepository]
    result(repository.collection.drop().toFuture(), timeout)
    result(repository.ensureIndexes(), timeout)
  }
}
