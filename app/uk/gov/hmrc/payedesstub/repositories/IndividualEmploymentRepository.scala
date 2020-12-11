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

package uk.gov.hmrc.payedesstub.repositories

import javax.inject.{Inject, Singleton}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.payedesstub.models.{IndividualEmployment, _}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndividualEmploymentRepository @Inject()(mongo: ReactiveMongoComponent)(implicit ec: ExecutionContext)
  extends ReactiveRepository[IndividualEmployment, BSONObjectID]("individualEmployment", mongo.mongoConnector.db,
    formatIndividualEmployment, formatObjectId) {

  def store[T <: IndividualEmployment](individualEmployment: T): Future[T] = {
    insert(individualEmployment) map {_ => individualEmployment}
  }

  def fetch(utr: String, taxYear: String): Future[Option[IndividualEmployment]] = {
    find("utr" -> utr, "taxYear" -> taxYear) map(_.headOption)
  }
}
