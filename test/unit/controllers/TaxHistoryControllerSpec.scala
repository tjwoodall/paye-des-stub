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

import common.LogSuppressing
import org.mockito.ArgumentMatchers.{any, anyString, eq => mEq}
import org.mockito.BDDMockito.given
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.payedesstub.controllers.TaxHistoryController
import uk.gov.hmrc.payedesstub.models._
import uk.gov.hmrc.payedesstub.services.{ScenarioLoader, TaxHistoryService}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxHistoryControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication with LogSuppressing {

  trait Setup {
    implicit lazy val materializer = fakeApplication.materializer
    implicit val hc = HeaderCarrier()

    def createTaxHistoryRequestV1 = FakeRequest().withHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/vnd.hmrc.1.0+json")

    def createTaxHistoryRequest = FakeRequest().withHeaders("Accept" -> "application/vnd.hmrc.2.0+json", "Content-Type" -> "application/vnd.hmrc.2.0+json")

    val underTest = new TaxHistoryController(mock[ScenarioLoader], mock[TaxHistoryService])

    def createSummaryRequestV1(scenario: String) = {
      createTaxHistoryRequestV1.withBody[JsValue](Json.parse(s"""{ "scenario": "$scenario" }"""))
    }

    def createSummaryRequest(scenario: String) = {
      createTaxHistoryRequest.withBody[JsValue](Json.parse(s"""{ "scenario": "$scenario" }"""))
    }

    def emptyRequest = {
      createTaxHistoryRequest.withBody[JsValue](Json.parse("{}"))
    }

    val taxYear = TaxYear("2016-17")
    val nino = Nino("AA000000A")
    val taxHistoryResponse =
      """|{
         |  "employments": []
         |}
      """.stripMargin
    val taxHistory = TaxHistory(nino.nino, taxYear.toString, taxHistoryResponse)
  }

  "find" should {
    "return 200 (Ok) with the response when called with a nino and taxYear that are found" in new Setup {

      given(underTest.service.fetch(nino, taxYear.startYr.toInt))
        .willReturn(Future(Some(TaxHistory("", "", taxHistoryResponse))))

      val result = await(underTest.find(nino, taxYear.startYr.toInt)(createTaxHistoryRequest))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe Json.parse(taxHistoryResponse)
    }

    "return 404 (NotFound) when called with a utr and taxYear that are not found" in new Setup {

      given(underTest.service.fetch(nino, taxYear.startYr.toInt)).willReturn(Future(None))

      val result = await(underTest.find(nino, taxYear.startYr.toInt)(createTaxHistoryRequest))

      status(result) shouldBe NOT_FOUND
    }
  }

  "create" should {
    "return 406 Not Acceptable when called with version 1" in new Setup {
      val result = await(underTest.create(nino, taxYear)(createSummaryRequestV1("EVERYTHING")))

      status(result) shouldBe NOT_ACCEPTABLE
      (jsonBodyOf(result) \ "code").as[String] shouldBe "ACCEPT_HEADER_INVALID"
    }

    "return a created response and store the tax history" in new Setup {

      given(underTest.scenarioLoader.loadScenarioRaw(mEq("tax-history"), mEq("EVERYTHING"))).willReturn(Future.successful(taxHistoryResponse))
      given(underTest.service.create(mEq(nino), mEq(taxYear), mEq(taxHistoryResponse))).willReturn(Future.successful(taxHistory))

      val result = await(underTest.create(nino, taxYear)(createSummaryRequest("EVERYTHING")))

      status(result) shouldBe CREATED
      bodyOf(result) shouldBe "{}"
    }

    "default to EVERYTHING Scenario when no scenario is specified in the request" in new Setup {

      given(underTest.scenarioLoader.loadScenarioRaw(mEq("tax-history"), mEq("EVERYTHING"))).willReturn(Future.successful(taxHistoryResponse))
      given(underTest.service.create(mEq(nino), mEq(taxYear), mEq(taxHistoryResponse))).willReturn(Future.successful(taxHistory))

      val result = await(underTest.create(nino, taxYear)(emptyRequest))

      status(result) shouldBe CREATED
      bodyOf(result) shouldBe "{}"
    }

    "return an Internal Server Error when the repository fails" in new Setup {

      given(underTest.scenarioLoader.loadScenarioRaw(mEq("tax-history"), mEq("EVERYTHING"))).willReturn(Future.successful(taxHistoryResponse))
      given(underTest.service.create(any(), any(), anyString)).willReturn(Future.failed(new RuntimeException("expected test error")))

      val result = await(underTest.create(nino, taxYear)(createSummaryRequest("EVERYTHING")))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return a bad request when the scenario is invalid" in new Setup {

      given(underTest.scenarioLoader.loadScenarioRaw(mEq("tax-history"), mEq("INVALID"))).willReturn(Future.failed(new InvalidScenarioException("INVALID")))

      val result = await(underTest.create(nino, taxYear)(createSummaryRequest("INVALID")))

      status(result) shouldBe BAD_REQUEST
      (jsonBodyOf(result) \ "code").as[String] shouldBe "UNKNOWN_SCENARIO"
    }
  }
}
