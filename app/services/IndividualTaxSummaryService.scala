/*
 * Copyright 2023 HM Revenue & Customs
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

package services

import models.{IndividualTax, IndividualTaxResponse}
import repositories.IndividualTaxRepository
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

@Singleton
class IndividualTaxSummaryService @Inject() (val repository: IndividualTaxRepository) {

  def create(utr: String, taxYear: String, individualTaxResponse: IndividualTaxResponse): Future[IndividualTax] =
    repository.store(IndividualTax(utr, taxYear, individualTaxResponse))

  def fetch(utr: String, taxYear: String): Future[Option[IndividualTax]] =
    repository.fetch(utr, taxYear)
}
