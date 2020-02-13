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

package uk.gov.hmrc.payedesstub.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.payedesstub.models._
import uk.gov.hmrc.payedesstub.services.{IndividualTaxSummaryService, ScenarioLoader}
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class IndividualTaxController @Inject()(val scenarioLoader: ScenarioLoader,
                                        val service: IndividualTaxSummaryService)
  extends BaseController with HeaderValidator {

  final def find(utr: String, taxYear: String): Action[AnyContent] = Action async {
    service.fetch(utr, taxYear) map {
      case Some(result) => Ok(Json.toJson(result.individualTaxResponse))
      case _ => NotFound
    } recover {
      case _ => InternalServerError
    }
  }

  final def create(utr: SaUtr, taxYear: TaxYear): Action[JsValue] = validateAcceptHeader("1.0").async(parse.json) { implicit request =>
    withJsonBody[CreateSummaryRequest] { createSummaryRequest =>

      val scenario = createSummaryRequest.scenario.getOrElse("HAPPY_PATH_1")

      for {
        individualTax <- scenarioLoader.loadScenario[IndividualTaxResponse]("individual-tax", scenario)
        _ <- service.create(utr.utr, taxYear.startYr, individualTax)
      } yield Created(Json.toJson(individualTax))

    } recover {
      case _: InvalidScenarioException => BadRequest(JsonErrorResponse("UNKNOWN_SCENARIO", "Unknown test scenario"))
      case _ => InternalServerError
    }
  }

}
