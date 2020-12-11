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
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.payedesstub.controllers.IndividualEmploymentController
import uk.gov.hmrc.payedesstub.models.{IndividualEmployment, IndividualEmploymentResponse, InvalidScenarioException, TaxYear}
import uk.gov.hmrc.payedesstub.services.{IndividualEmploymentSummaryService, ScenarioLoader}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndividualEmploymentControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  trait Setup {
    implicit lazy val materializer = fakeApplication.materializer
    implicit val hc = HeaderCarrier()

    def createIndividualEmploymentRequest = FakeRequest()
      .withHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/vnd.hmrc.1.0+json")

    val underTest = new IndividualEmploymentController(
      mock[ScenarioLoader],
      mock[IndividualEmploymentSummaryService],
      stubControllerComponents()
    )

    def createSummaryRequest(scenario: String) = {
      createIndividualEmploymentRequest.withBody[JsValue](Json.parse(s"""{ "scenario": "$scenario" }"""))
    }

    def emptyRequest = {
      createIndividualEmploymentRequest.withBody[JsValue](Json.parse("{}"))
    }

    val validUtrString = "2234567890"
    val validTaxYearString = "2016-17"
    val utr = SaUtr(validUtrString)
    val taxYear = TaxYear(validTaxYearString)
    val individualEmploymentResponse = IndividualEmploymentResponse(Nil)
    val individualEmployment = IndividualEmployment("", "", individualEmploymentResponse)
  }

  "find" should {
    "return 200 (Ok) with the happy path response when called with a utr and taxYear that are found" in new Setup {

      given(underTest.service.fetch(validUtrString, validTaxYearString))
        .willReturn(Future(Some(IndividualEmployment("", "", IndividualEmploymentResponse(Nil)))))

      val result = await(underTest.find(validUtrString, validTaxYearString)(createIndividualEmploymentRequest))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe Json.toJson(individualEmploymentResponse)
    }

    "return 404 (NotFound) when called with a utr and taxYear that are not found" in new Setup {

      given(underTest.service.fetch(validUtrString, validTaxYearString)).willReturn(Future(None))

      val result = await(underTest.find(validUtrString, validTaxYearString)(createIndividualEmploymentRequest))

      status(result) shouldBe NOT_FOUND
    }
  }

  "create" should {

    "return a created response and store the Individual Employment summary" in new Setup {

      given(underTest.scenarioLoader.loadScenario[IndividualEmploymentResponse](anyString, anyString)(any()))
        .willReturn(Future.successful(individualEmploymentResponse))
      given(underTest.service.create(anyString, anyString, any[IndividualEmploymentResponse]))
        .willReturn(Future.successful(individualEmployment))

      val result = await(underTest.create(utr, taxYear)(createSummaryRequest("HAPPY_PATH_1")))

      status(result) shouldBe CREATED
      verify(underTest.scenarioLoader).loadScenario[IndividualEmploymentResponse]("individual-employment", "HAPPY_PATH_1")
      verify(underTest.service).create(validUtrString, taxYear.startYr, individualEmploymentResponse)
    }

    "default to Happy Path Scenario 1 when no scenario is specified in the request" in new Setup {

      given(underTest.scenarioLoader.loadScenario[IndividualEmploymentResponse](anyString, anyString)(any()))
        .willReturn(Future.successful(individualEmploymentResponse))
      given(underTest.service.create(anyString, anyString, any[IndividualEmploymentResponse]))
        .willReturn(Future.successful(individualEmployment))

      val result = await(underTest.create(utr, taxYear)(emptyRequest))

      status(result) shouldBe CREATED
      verify(underTest.scenarioLoader).loadScenario[IndividualEmploymentResponse]("individual-employment", "HAPPY_PATH_1")
      verify(underTest.service).create(validUtrString, taxYear.startYr, individualEmploymentResponse)
    }

    "return an invalid server error when the repository fails" in new Setup {

      given(underTest.scenarioLoader.loadScenario[IndividualEmploymentResponse](anyString, anyString)(any()))
        .willReturn(Future.successful(individualEmploymentResponse))
      given(underTest.service.create(anyString, anyString, any[IndividualEmploymentResponse]))
        .willReturn(Future.failed(new RuntimeException("expected test error")))

      val result = await(underTest.create(utr, taxYear)(createSummaryRequest("HAPPY_PATH_1")))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return a bad request when the scenario is invalid" in new Setup {

      given(underTest.scenarioLoader.loadScenario[IndividualEmploymentResponse](anyString, anyString)(any()))
        .willReturn(Future.failed(new InvalidScenarioException("INVALID")))

      val result = await(underTest.create(utr, taxYear)(createSummaryRequest("INVALID")))

      status(result) shouldBe BAD_REQUEST
      (jsonBodyOf(result) \ "code").as[String] shouldBe "UNKNOWN_SCENARIO"
    }
  }
}

