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

import helpers.BaseSpec
import org.mongodb.scala.SingleObservableFuture
import play.api.http.Status.{CREATED, NOT_FOUND, OK}
import play.api.libs.ws.StandaloneWSRequest
import repositories.IndividualBenefitsRepository

import scala.concurrent.Await.result

class IndividualBenefitsSpec extends BaseSpec {
  Feature("Fetch individual benefits summary data") {
    Scenario("No data is returned because utr and taxYear are not found") {
      When("I request benefits summary data for a given utr and taxYear")
      val response = fetchIndividualBenefitsData("1111111111", "2016-17")

      Then("The response should indicate that no data was found")
      response.status shouldBe NOT_FOUND
    }

    Scenario("Benefits summary data can be primed") {
      When("I request benefits summary data for a given utr and taxYear")
      val response = primeIndividualBenefitsData("1111111111", "2016-17", """{ "scenario": "HAPPY_PATH_1" }""")

      Then("The response should indicate that the summary has been created")
      response.status shouldBe CREATED
    }

    Scenario(
      "Individual benefits summary data is returned for the given utr and taxYear when primed with the default scenario"
    ) {
      When("I prime tax data for a given utr and taxYear")
      val primeResponse = primeIndividualBenefitsData("1111111111", "2016-17", "{}")

      Then("The response should contain individual benefits data")
      primeResponse.status shouldBe CREATED

      And("I request employment data for a given utr and taxYear")
      val fetchResponse = fetchIndividualBenefitsData("1111111111", "2016")

      And("The response should contain individual benefits data")
      fetchResponse.status shouldBe OK
    }

    Scenario(
      "Individual benefits summary data is returned for the given ut and taxYear when primed with a specific scenario"
    ) {
      When("I prime tax data for a given utr, taxYear and test scenario")
      val primeResponse = primeIndividualBenefitsData("1111111111", "2016-17", """{"scenario":"HAPPY_PATH_1"}""")

      Then("The response should contain individual benefits data")
      primeResponse.status shouldBe CREATED

      And("I request employment data for a given utr and taxYear")
      val fetchResponse = fetchIndividualBenefitsData("1111111111", "2016")

      And("The response should contain individual benefits data")
      fetchResponse.status shouldBe OK
    }
  }

  private def primeIndividualBenefitsData(
    utr: String,
    taxYear: String,
    payload: String
  ): StandaloneWSRequest#Response =
    postEndpoint(s"sa/$utr/benefits/annual-summary/$taxYear", payload)

  private def fetchIndividualBenefitsData(utr: String, taxYear: String): StandaloneWSRequest#Response =
    getEndpoint(s"self-assessment-prepop/individual/$utr/benefits/tax-year/$taxYear")

  override protected def beforeEach(): Unit = {
    val repository = app.injector.instanceOf[IndividualBenefitsRepository]
    result(repository.collection.drop().toFuture(), timeout)
    result(repository.ensureIndexes(), timeout)
  }
}
