/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.payedesstub

import play.api.libs.json.Json
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

package object models {
  implicit val formatObjectId = ReactiveMongoFormats.objectIdFormats

  implicit val createSummaryRequest = Json.format[CreateSummaryRequest]

  implicit val refund = Json.format[Refund]
  implicit val extendedStateBenefits = Json.format[ExtendedStateBenefits]
  implicit val stateBenefits = Json.format[StateBenefits]
  implicit val individualEmploymentEmployment = Json.format[IndividualEmploymentEmployment]
  implicit val individualIncomeEmployment = Json.format[IndividualIncomeEmployment]
  implicit val individualTaxEmployment = Json.format[IndividualTaxEmployment]
  implicit val individualBenefitsEmployment = Json.format[IndividualBenefitsEmployment]
  implicit val individualTaxResponse = Json.format[IndividualTaxResponse]
  implicit val individualIncomeResponse = Json.format[IndividualIncomeResponse]
  implicit val individualEmploymentResponse = Json.format[IndividualEmploymentResponse]
  implicit val individualBenefitsResponse = Json.format[IndividualBenefitsResponse]

  implicit val formatIndividualEmployment = Json.format[IndividualEmployment]
  implicit val formatIndividualIncome = Json.format[IndividualIncome]
  implicit val formatIndividualTax = Json.format[IndividualTax]
  implicit val formatIndividualBenefits = Json.format[IndividualBenefits]

  implicit val formatTaxHistory = Json.format[TaxHistory]

  implicit val apiAccessFmt = Json.format[APIAccess]
}
