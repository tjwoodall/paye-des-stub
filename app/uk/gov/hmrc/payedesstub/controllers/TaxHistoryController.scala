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

package uk.gov.hmrc.payedesstub.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.payedesstub.models._
import uk.gov.hmrc.payedesstub.services.{ScenarioLoader, TaxHistoryService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class TaxHistoryController @Inject() (
  val scenarioLoader: ScenarioLoader,
  val service: TaxHistoryService,
  val cc: ControllerComponents
) extends BackendController(cc)
    with HeaderValidator {

  implicit val ec: ExecutionContext = cc.executionContext

  final def find(nino: Nino, taxYear: Int): Action[AnyContent] = Action async {
    service.fetch(nino, taxYear) map {
      case Some(result) => Ok(result.taxHistoryResponse)
      case _            => NotFound
    } recover { case _ =>
      InternalServerError
    }
  }

  final def create(nino: Nino, taxYear: TaxYear): Action[JsValue] =
    (cc.actionBuilder andThen validateAcceptHeader("2.0")).async(parse.json) { implicit request =>
      withJsonBody[CreateSummaryRequest] { createSummaryRequest =>
        val scenario = createSummaryRequest.scenario.getOrElse("EVERYTHING")

        for {
          taxHistoryResponse <- scenarioLoader.loadScenarioRaw("tax-history", scenario)
          _                  <- service.create(nino, taxYear, taxHistoryResponse)
        } yield Created("{}").withHeaders(CONTENT_TYPE -> JSON)

      } recover {
        case _: InvalidScenarioException => BadRequest(JsonErrorResponse("UNKNOWN_SCENARIO", "Unknown test scenario"))
        case _                           => InternalServerError
      }
    }

}
