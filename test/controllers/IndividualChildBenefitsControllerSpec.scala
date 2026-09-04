/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers

import models.*
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{IndividualChildBenefitsSummaryService, ScenarioLoader}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndividualChildBenefitsControllerSpec
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with ScalaFutures
    with GuiceOneAppPerSuite {

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    def createIndividualChildBenefitsRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      .withHeaders("Accept" -> "application/vnd.hmrc.2.0+json", "Content-Type" -> "application/vnd.hmrc.1.0+json")

    def createIndividualChildBenefitsRequestAccept2_1: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
      .withHeaders("Accept" -> "application/vnd.hmrc.2.1+json", "Content-Type" -> "application/vnd.hmrc.1.0+json")

    val underTest = new IndividualChildBenefitsController(
      Mockito.mock(classOf[ScenarioLoader]),
      Mockito.mock(classOf[IndividualChildBenefitsSummaryService]),
      stubControllerComponents()
    )

    def createSummaryRequest(scenario: String): FakeRequest[JsValue] =
      createIndividualChildBenefitsRequest.withBody[JsValue](Json.parse(s"""{ "scenario": "$scenario" }"""))

    def createSummaryRequestAccept2_1(scenario: String): FakeRequest[JsValue] =
      createIndividualChildBenefitsRequestAccept2_1.withBody[JsValue](Json.parse(s"""{ "scenario": "$scenario" }"""))

    def emptyRequest: FakeRequest[JsValue] =
      createIndividualChildBenefitsRequest.withBody[JsValue](Json.parse("{}"))

    val validUtrString                                                           = "2234567890"
    val validTaxYearString                                                       = "2016-17"
    val utr: SaUtr                                                               = SaUtr(validUtrString)
    val taxYear: TaxYear                                                         = TaxYear(validTaxYearString)
    val individualChildBenefitsResponse: IndividualChildBenefitsResponse         = IndividualChildBenefitsResponse(
      Seq(IndividualChildBenefitsResponseDetail(BigDecimal(23.33)))
    )
    val individualChildBenefitsPostResponse: IndividualChildBenefitsPostResponse = IndividualChildBenefitsPostResponse(
      expectedStatus = 200,
      expectedJson = Some(Json.obj("test" -> "test"))
    )
    val individualChildBenefits500Response: IndividualChildBenefitsResponse      =
      IndividualChildBenefitsResponse(Nil, Some(500))
    val individualChildBenefits: IndividualChildBenefits                         =
      IndividualChildBenefits("", "", individualChildBenefitsResponse)
  }

  "fetch" should {
    "return 200 (OK) with the happy path response when called with a utr and taxYear that are found" in new Setup {

      `given`(underTest.service.fetch(validUtrString, validTaxYearString))
        .willReturn(
          Future(
            Some(
              IndividualChildBenefits(
                "",
                "",
                IndividualChildBenefitsResponse(Seq(IndividualChildBenefitsResponseDetail(BigDecimal(23.33))))
              )
            )
          )
        )

      val result: Future[Result] =
        Future(underTest.find(validUtrString, validTaxYearString)(createIndividualChildBenefitsRequest)).futureValue

      status(result) shouldBe OK
    }

    "return 404 (NOT_FOUND) when called with a utr and taxYear that are not found" in new Setup {

      `given`(underTest.service.fetch(validUtrString, validTaxYearString)).willReturn(Future(None))

      val result: Future[Result] =
        Future(underTest.find(validUtrString, validTaxYearString)(createIndividualChildBenefitsRequest)).futureValue

      status(result) shouldBe NOT_FOUND
    }

    "return 500 (INTERNAL_SERVER_ERROR) for failure from a GatewayTimeoutException" in new Setup {

      `given`(underTest.service.fetch(validUtrString, validTaxYearString))
        .willReturn(Future.failed(new GatewayTimeoutException("Expected timeout")))

      val result: Future[Result] =
        Future(underTest.find(validUtrString, validTaxYearString)(createIndividualChildBenefitsRequest)).futureValue

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "create" should {

    "return a created response and store the Child Benefits Entitlement" in new Setup {
      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadHICBC(anyString, anyString))
        .willReturn(Future.successful(Tuple2(individualChildBenefitsResponse, individualChildBenefitsPostResponse)))
      `given`(underTest.service.create(anyString, anyString, any[IndividualChildBenefitsResponse]))
        .willReturn(Future.successful(individualChildBenefits))

      val result: Future[Result] =
        Future(underTest.create(utr, taxYear)(createSummaryRequest("HAPPY_PATH_1"))).futureValue

      status(result)        shouldBe CREATED
      contentAsJson(result) shouldBe Json.toJson(individualChildBenefitsPostResponse)
      verify(underTest.scenarioLoader)
        .loadScenarioWithTransformedPayloadHICBC("individual-child-benefits", "HAPPY_PATH_1")
      verify(underTest.service).create(validUtrString, taxYear.endYr, individualChildBenefitsResponse)
    }
    "return a created response and store the Child Benefits Entitlement for unhappy path" in new Setup {
      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadHICBC(anyString, anyString))
        .willReturn(Future.successful(Tuple2(individualChildBenefitsResponse, individualChildBenefitsPostResponse)))
      `given`(underTest.service.create(anyString, anyString, any[IndividualChildBenefitsResponse]))
        .willReturn(Future.successful(individualChildBenefits))

      val result: Future[Result] =
        Future(underTest.create(utr, taxYear)(createSummaryRequest("UNHAPPY_PATH_500"))).futureValue

      status(result)        shouldBe CREATED
      contentAsJson(result) shouldBe Json.toJson(IndividualChildBenefitsPostResponse(500))
      verify(underTest.service).create(validUtrString, taxYear.endYr, individualChildBenefits500Response)
    }

    "return a created response and store the Child Benefits Entitlement for accept 2.1" in new Setup {

      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadHICBC(anyString, anyString))
        .willReturn(Future.successful(Tuple2(individualChildBenefitsResponse, individualChildBenefitsPostResponse)))
      `given`(underTest.service.create(anyString, anyString, any[IndividualChildBenefitsResponse]))
        .willReturn(Future.successful(individualChildBenefits))

      val result: Future[Result] =
        Future(underTest.create(utr, taxYear)(createSummaryRequestAccept2_1("HAPPY_PATH_1"))).futureValue

      status(result) shouldBe CREATED
      contentAsJson(result) shouldBe Json.toJson(individualChildBenefitsPostResponse)
      verify(underTest.scenarioLoader)
        .loadScenarioWithTransformedPayloadHICBC("individual-child-benefits", "HAPPY_PATH_1")
      verify(underTest.service).create(validUtrString, taxYear.endYr, individualChildBenefitsResponse)
    }

    "default to Happy Path Scenario 1 when no scenario is specified in the request" in new Setup {

      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadHICBC(anyString, anyString))
        .willReturn(Future.successful(Tuple2(individualChildBenefitsResponse, individualChildBenefitsPostResponse)))
      `given`(underTest.service.create(anyString, anyString, any[IndividualChildBenefitsResponse]))
        .willReturn(Future.successful(individualChildBenefits))

      val result: Future[Result] = Future(underTest.create(utr, taxYear)(emptyRequest)).futureValue

      status(result)        shouldBe CREATED
      contentAsJson(result) shouldBe Json.toJson(individualChildBenefitsPostResponse)
      verify(underTest.scenarioLoader)
        .loadScenarioWithTransformedPayloadHICBC("individual-child-benefits", "HAPPY_PATH_1")
      verify(underTest.service).create(validUtrString, taxYear.endYr, individualChildBenefitsResponse)
    }

    "return an invalid server error when the repository fails" in new Setup {

      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadHICBC(anyString, anyString))
        .willReturn(Future.successful(Tuple2(individualChildBenefitsResponse, individualChildBenefitsPostResponse)))
      `given`(underTest.service.create(anyString, anyString, any[IndividualChildBenefitsResponse]))
        .willReturn(Future.failed(new RuntimeException("expected test error")))

      val result: Future[Result] =
        Future(underTest.create(utr, taxYear)(createSummaryRequest("HAPPY_PATH_1"))).futureValue

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 406 (NOT_ACCEPTABLE) for an invalid accept header" in new Setup {

      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadHICBC(anyString, anyString))
        .willReturn(Future.successful(Tuple2(individualChildBenefitsResponse, individualChildBenefitsPostResponse)))
      `given`(underTest.service.create(anyString, anyString, any[IndividualChildBenefitsResponse]))
        .willReturn(Future.successful(individualChildBenefits))

      val result: Future[Result] = Future(
        underTest.create(utr, taxYear)(emptyRequest.withHeaders("Accept" -> "application/vnd.hmrc.0.9+json"))
      ).futureValue

      status(result) shouldBe NOT_ACCEPTABLE
    }

    "return a bad request when the scenario is invalid" in new Setup {

      `given`(underTest.scenarioLoader.loadScenarioWithTransformedPayloadHICBC(anyString, anyString))
        .willReturn(Future.failed(new InvalidScenarioException("INVALID")))

      val result: Future[Result] = Future(underTest.create(utr, taxYear)(createSummaryRequest("INVALID"))).futureValue

      status(result)                              shouldBe BAD_REQUEST
      (contentAsJson(result) \ "code").as[String] shouldBe "UNKNOWN_SCENARIO"
    }
  }
}
