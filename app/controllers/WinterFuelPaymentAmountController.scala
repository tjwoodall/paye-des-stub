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
import play.api.Logging
import play.api.libs.json.*
import play.api.mvc.*
import services.{IndividualChildBenefitsSummaryService, ScenarioLoader, WinterFuelPaymentAmountSummaryService}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class WinterFuelPaymentAmountController @Inject() (
  val scenarioLoader: ScenarioLoader,
  val service: WinterFuelPaymentAmountSummaryService,
  val cc: ControllerComponents
) extends BackendController(cc)
    with HeaderValidator
    with Logging {

  implicit val ec: ExecutionContext = cc.executionContext

  final def find(nino: String, taxYear: String): Action[AnyContent] = Action async {
    service.fetch(nino, taxYear) map {
      case Some(result) =>
        result.winterFuelPaymentAmountResponse.errorResponse match {
          case Some(errorResponse) => Status(errorResponse)
          case None                => Ok(Json.toJson(result.winterFuelPaymentAmountResponse))
        }

      case _ => NotFound
    } recover { case e =>
      logger.error("[WinterFuelPaymentAmountController][find] An error occurred while finding test data", e)
      InternalServerError
    }
  }

  final def create(nino: Nino, taxYear: TaxYear): Action[JsValue] =
    (cc.actionBuilder andThen validateAcceptHeader(supportedVersions*)).async(parse.json) { request =>
      given Request[JsValue] = request
      withJsonBody[CreateSummaryRequest] { (createSummaryRequest: CreateSummaryRequest) =>
        val scenario = createSummaryRequest.scenario.getOrElse("HAPPY_PATH_1")
        if (scenario.startsWith("UNHAPPY_PATH_")) {
          val errorResponseStatus             = scenario.split("_")(2).toInt
          val winterFuelPaymentAmountResponse =
            WinterFuelPaymentAmountResponse(Nil, Some(errorResponseStatus))
          service
            .create(nino.nino, taxYear.startYr, winterFuelPaymentAmountResponse)
            .map(_ => Created(Json.toJson(WinterFuelPaymentAmountPostResponse(expectedStatus = errorResponseStatus))))
        } else {
          for {
            Tuple2(winterFuelPaymentAmountResponse, winterFuelPaymentAmountPostResponse) <-
              scenarioLoader.loadScenarioWithTransformedPayloadWFPA("winter-fuel-payment-amount", scenario)
            _                                                                            <- service.create(nino.nino, taxYear.startYr, winterFuelPaymentAmountResponse)
          } yield Created(Json.toJson(winterFuelPaymentAmountPostResponse))
        }

      } recover {
        case _: InvalidScenarioException => BadRequest(JsonErrorResponse("UNKNOWN_SCENARIO", "Unknown test scenario"))
        case _                           => InternalServerError
      }
    }

}
