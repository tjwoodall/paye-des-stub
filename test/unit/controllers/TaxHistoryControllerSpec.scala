/*
 * Copyright 2021 HM Revenue & Customs
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

import akka.stream.Materializer
import common.LogSuppressing
import org.mockito.ArgumentMatchers.{any, anyString, eq => mEq}
import org.mockito.BDDMockito.given
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{stubControllerComponents, _}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.payedesstub.controllers.TaxHistoryController
import uk.gov.hmrc.payedesstub.models._
import uk.gov.hmrc.payedesstub.services.{ScenarioLoader, TaxHistoryService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxHistoryControllerSpec extends AnyWordSpecLike with Matchers with OptionValues
  with MockitoSugar with ScalaFutures with GuiceOneAppPerSuite with LogSuppressing {

  trait Setup {
    implicit lazy val materializer: Materializer = fakeApplication.materializer
    implicit val hc: HeaderCarrier = HeaderCarrier()

    def createTaxHistoryRequestV1: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().
      withHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/vnd.hmrc.1.0+json")

    def createTaxHistoryRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().
      withHeaders("Accept" -> "application/vnd.hmrc.2.0+json", "Content-Type" -> "application/vnd.hmrc.2.0+json")

    val underTest = new TaxHistoryController(mock[ScenarioLoader], mock[TaxHistoryService],
      stubControllerComponents()
    )

    def createSummaryRequestV1(scenario: String): FakeRequest[JsValue] = {
      createTaxHistoryRequestV1.withBody[JsValue](Json.parse(s"""{ "scenario": "$scenario" }"""))
    }

    def createSummaryRequest(scenario: String): FakeRequest[JsValue] = {
      createTaxHistoryRequest.withBody[JsValue](Json.parse(s"""{ "scenario": "$scenario" }"""))
    }

    def emptyRequest: FakeRequest[JsValue] = {
      createTaxHistoryRequest.withBody[JsValue](Json.parse("{}"))
    }

    val taxYear: TaxYear = TaxYear("2016-17")
    val nino: Nino = Nino("AA000000A")
    val taxHistoryResponse: String =
      """|{
         |  "employments": []
         |}
      """.stripMargin
    val taxHistory: TaxHistory = TaxHistory(nino.nino, taxYear.toString, taxHistoryResponse)
  }

  "find" should {
    "return 200 (Ok) with the response when called with a nino and taxYear that are found" in new Setup {

      given(underTest.service.fetch(nino, taxYear.startYr.toInt))
        .willReturn(Future(Some(TaxHistory("", "", taxHistoryResponse))))

      val result: Future[Result] = Future(underTest.find(nino, taxYear.startYr.toInt)(createTaxHistoryRequest)).futureValue

      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.parse(taxHistoryResponse)
    }

    "return 404 (NotFound) when called with a utr and taxYear that are not found" in new Setup {

      given(underTest.service.fetch(nino, taxYear.startYr.toInt)).willReturn(Future(None))

      val result: Future[Result] = Future(underTest.find(nino, taxYear.startYr.toInt)(createTaxHistoryRequest)).futureValue

      status(result) shouldBe NOT_FOUND
    }
  }

  "create" should {
    "return 406 Not Acceptable when called with version 1" in new Setup {
      val result: Future[Result] = Future(underTest.create(nino, taxYear)(createSummaryRequestV1("EVERYTHING"))).futureValue

      status(result) shouldBe NOT_ACCEPTABLE
      (contentAsJson(result) \ "code").as[String] shouldBe "ACCEPT_HEADER_INVALID"
    }

    "return a created response and store the tax history" in new Setup {

      given(underTest.scenarioLoader.loadScenarioRaw(mEq("tax-history"), mEq("EVERYTHING"))).willReturn(Future.successful(taxHistoryResponse))
      given(underTest.service.create(mEq(nino), mEq(taxYear), mEq(taxHistoryResponse))).willReturn(Future.successful(taxHistory))

      val result: Future[Result] = Future(underTest.create(nino, taxYear)(createSummaryRequest("EVERYTHING"))).futureValue

      status(result) shouldBe CREATED
      contentAsString(result) shouldBe "{}"
    }

    "default to EVERYTHING Scenario when no scenario is specified in the request" in new Setup {

      given(underTest.scenarioLoader.loadScenarioRaw(mEq("tax-history"), mEq("EVERYTHING"))).willReturn(Future.successful(taxHistoryResponse))
      given(underTest.service.create(mEq(nino), mEq(taxYear), mEq(taxHistoryResponse))).willReturn(Future.successful(taxHistory))

      val result: Future[Result] = Future(underTest.create(nino, taxYear)(emptyRequest)).futureValue

      status(result) shouldBe CREATED
      contentAsString(result) shouldBe "{}"
    }

    "return an Internal Server Error when the repository fails" in new Setup {

      given(underTest.scenarioLoader.loadScenarioRaw(mEq("tax-history"), mEq("EVERYTHING"))).willReturn(Future.successful(taxHistoryResponse))
      given(underTest.service.create(any(), any(), anyString)).willReturn(Future.failed(new RuntimeException("expected test error")))

      val result: Future[Result] = Future(underTest.create(nino, taxYear)(createSummaryRequest("EVERYTHING"))).futureValue

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return a bad request when the scenario is invalid" in new Setup {

      given(underTest.scenarioLoader.loadScenarioRaw(mEq("tax-history"), mEq("INVALID"))).willReturn(Future.failed(new InvalidScenarioException("INVALID")))

      val result: Future[Result] = Future(underTest.create(nino, taxYear)(createSummaryRequest("INVALID"))).futureValue

      status(result) shouldBe BAD_REQUEST
      (contentAsJson(result) \ "code").as[String] shouldBe "UNKNOWN_SCENARIO"
    }
  }
}
