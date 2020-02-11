/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.controllers

import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.payedesstub.controllers.IndividualBenefitsController
import uk.gov.hmrc.payedesstub.models.{IndividualBenefits, IndividualBenefitsResponse, InvalidScenarioException, TaxYear}
import uk.gov.hmrc.payedesstub.services.{IndividualBenefitsSummaryService, ScenarioLoader}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndividualBenefitsControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  trait Setup {
    implicit lazy val materializer = fakeApplication.materializer
    implicit val hc = HeaderCarrier()

    def createIndividualBenefitsRequest = FakeRequest()
      .withHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/vnd.hmrc.1.0+json")

    val underTest = new IndividualBenefitsController(mock[ScenarioLoader], mock[IndividualBenefitsSummaryService])

    def createSummaryRequest(scenario: String) = {
      createIndividualBenefitsRequest.withBody[JsValue](Json.parse(s"""{ "scenario": "$scenario" }"""))
    }

    def emptyRequest = {
      createIndividualBenefitsRequest.withBody[JsValue](Json.parse("{}"))
    }

    val validUtrString = "2234567890"
    val validTaxYearString = "2016-17"
    val utr = SaUtr(validUtrString)
    val taxYear = TaxYear(validTaxYearString)
    val individualBenefitsResponse = IndividualBenefitsResponse(Nil)
    val individualBenefits = IndividualBenefits("", "", individualBenefitsResponse)
  }

  "fetch" should {
    "return 200 (Ok) with the happy path response when called with a utr and taxYear that are found" in new Setup {

      given(underTest.service.fetch(validUtrString, validTaxYearString))
        .willReturn(Future(Some(IndividualBenefits("", "", IndividualBenefitsResponse(Nil)))))

      val result = await(underTest.find(validUtrString, validTaxYearString)(createIndividualBenefitsRequest))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe Json.toJson(individualBenefitsResponse)
    }

    "return 404 (NotFound) when called with a utr and taxYear that are not found" in new Setup {

      given(underTest.service.fetch(validUtrString, validTaxYearString)).willReturn(Future(None))

      val result = await(underTest.find(validUtrString, validTaxYearString)(createIndividualBenefitsRequest))

      status(result) shouldBe NOT_FOUND
    }
  }

  "create" should {

    "return a created response and store the Individual Benefits summary" in new Setup {

      given(underTest.scenarioLoader.loadScenario[IndividualBenefitsResponse](anyString, anyString)(any()))
        .willReturn(Future.successful(individualBenefitsResponse))
      given(underTest.service.create(anyString, anyString, any[IndividualBenefitsResponse]))
        .willReturn(Future.successful(individualBenefits))

      val result = await(underTest.create(utr, taxYear)(createSummaryRequest("HAPPY_PATH_1")))

      status(result) shouldBe CREATED
      verify(underTest.scenarioLoader).loadScenario[IndividualBenefitsResponse]("individual-benefits", "HAPPY_PATH_1")
      verify(underTest.service).create(validUtrString, taxYear.startYr, individualBenefitsResponse)
    }

    "default to Happy Path Scenario 1 when no scenario is specified in the request" in new Setup {

      given(underTest.scenarioLoader.loadScenario[IndividualBenefitsResponse](anyString, anyString)(any()))
        .willReturn(Future.successful(individualBenefitsResponse))
      given(underTest.service.create(anyString, anyString, any[IndividualBenefitsResponse]))
        .willReturn(Future.successful(individualBenefits))

      val result = await(underTest.create(utr, taxYear)(emptyRequest))

      status(result) shouldBe CREATED
      verify(underTest.scenarioLoader).loadScenario[IndividualBenefitsResponse]("individual-benefits", "HAPPY_PATH_1")
      verify(underTest.service).create(validUtrString, taxYear.startYr, individualBenefitsResponse)
    }

    "return an invalid server error when the repository fails" in new Setup {

      given(underTest.scenarioLoader.loadScenario[IndividualBenefitsResponse](anyString, anyString)(any()))
        .willReturn(Future.successful(individualBenefitsResponse))
      given(underTest.service.create(anyString, anyString, any[IndividualBenefitsResponse]))
        .willReturn(Future.failed(new RuntimeException("expected test error")))

      val result = await(underTest.create(utr, taxYear)(createSummaryRequest("HAPPY_PATH_1")))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return a bad request when the scenario is invalid" in new Setup {

      given(underTest.scenarioLoader.loadScenario[IndividualBenefitsResponse](anyString, anyString)(any()))
        .willReturn(Future.failed(new InvalidScenarioException("INVALID")))

      val result = await(underTest.create(utr, taxYear)(createSummaryRequest("INVALID")))

      status(result) shouldBe BAD_REQUEST
      (jsonBodyOf(result) \ "code").as[String] shouldBe "UNKNOWN_SCENARIO"
    }
  }
}
