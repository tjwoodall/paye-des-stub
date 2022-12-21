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

package services

import models.{IndividualEmployment, IndividualEmploymentResponse}
import repositories.IndividualEmploymentRepository
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

@Singleton
class IndividualEmploymentSummaryService @Inject() (val repository: IndividualEmploymentRepository) {

  def create(
    utr: String,
    taxYear: String,
    individualEmploymentResponse: IndividualEmploymentResponse
  ): Future[IndividualEmployment] =
    repository.store(IndividualEmployment(utr, taxYear, individualEmploymentResponse))

  def fetch(utr: String, taxYear: String): Future[Option[IndividualEmployment]] =
    repository.fetch(utr, taxYear)
}
