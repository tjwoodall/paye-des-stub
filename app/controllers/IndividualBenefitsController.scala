/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import models._
import services.{IndividualBenefitsSummaryService, ScenarioLoader}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class IndividualBenefitsController @Inject() (
  val scenarioLoader: ScenarioLoader,
  val service: IndividualBenefitsSummaryService,
  val cc: ControllerComponents
) extends BackendController(cc)
    with HeaderValidator {

  implicit val ec: ExecutionContext = cc.executionContext

  private val logger = Logger(this.getClass)

  final def find(utr: String, taxYear: String): Action[AnyContent] = Action async {
    service.fetch(utr, taxYear) map {
      case Some(result) => Ok(Json.toJson(result.individualBenefitsResponse))
      case _            => NotFound
    } recover { case e =>
      logger.error("An error occurred while finding test data", e)
      InternalServerError
    }
  }

  final def create(utr: SaUtr, taxYear: TaxYear): Action[JsValue] =
    (cc.actionBuilder andThen validateAcceptHeader("1.0")).async(parse.json) { implicit request =>
      withJsonBody[CreateSummaryRequest] { createSummaryRequest =>
        val scenario = createSummaryRequest.scenario.getOrElse("HAPPY_PATH_1")

        for {
          individualBenefits <- scenarioLoader.loadScenario[IndividualBenefitsResponse]("individual-benefits", scenario)
          _                  <- service.create(utr.utr, taxYear.startYr, individualBenefits)
        } yield Created(Json.toJson(individualBenefits))

      } recover {
        case _: InvalidScenarioException => BadRequest(JsonErrorResponse("UNKNOWN_SCENARIO", "Unknown test scenario"))
        case _                           => InternalServerError
      }
    }

}
