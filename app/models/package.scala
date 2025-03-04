/*
 * Copyright 2025 HM Revenue & Customs
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

import org.bson.types.ObjectId
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoFormats
package object models {
  implicit val doubleWrite: Writes[Double] = (value: Double) =>
    JsNumber(
      BigDecimal(value).setScale(2, BigDecimal.RoundingMode.FLOOR)
    )

  implicit val formatObjectId: Format[ObjectId] = MongoFormats.objectIdFormat

  implicit val createSummaryRequest: OFormat[CreateSummaryRequest] = Json.format[CreateSummaryRequest]

  implicit val refund: OFormat[Refund]                                                 = Json.format[Refund]
  implicit val extendedStateBenefits: OFormat[ExtendedStateBenefits]                   = Json.format[ExtendedStateBenefits]
  implicit val stateBenefits: OFormat[StateBenefits]                                   = Json.format[StateBenefits]
  implicit val individualEmploymentEmployment: OFormat[IndividualEmploymentEmployment] =
    Json.format[IndividualEmploymentEmployment]
  implicit val individualIncomeEmployment: OFormat[IndividualIncomeEmployment]         = Json.format[IndividualIncomeEmployment]
  implicit val individualTaxEmployment: OFormat[IndividualTaxEmployment]               = Json.format[IndividualTaxEmployment]
  implicit val individualBenefitsEmployment: OFormat[IndividualBenefitsEmployment]     =
    Json.format[IndividualBenefitsEmployment]
  implicit val individualTaxResponse: OFormat[IndividualTaxResponse]                   = Json.format[IndividualTaxResponse]
  implicit val individualIncomeResponse: OFormat[IndividualIncomeResponse]             = Json.format[IndividualIncomeResponse]
  implicit val individualEmploymentResponse: OFormat[IndividualEmploymentResponse]     =
    Json.format[IndividualEmploymentResponse]
  implicit val individualBenefitsResponse: OFormat[IndividualBenefitsResponse]         = Json.format[IndividualBenefitsResponse]

  implicit val formatIndividualEmployment: OFormat[IndividualEmployment] = Json.format[IndividualEmployment]
  implicit val formatIndividualIncome: OFormat[IndividualIncome]         = Json.format[IndividualIncome]
  implicit val formatIndividualTax: OFormat[IndividualTax]               = Json.format[IndividualTax]
  implicit val formatIndividualBenefits: OFormat[IndividualBenefits]     = Json.format[IndividualBenefits]

}
