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

package uk.gov.hmrc.payedesstub.repositories

import javax.inject.{Inject, Singleton}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.payedesstub.models._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxHistoryRepository @Inject()(mongo: ReactiveMongoComponent)(implicit ec: ExecutionContext)
  extends ReactiveRepository[TaxHistory, BSONObjectID]("taxHistory", mongo.mongoConnector.db,
    formatTaxHistory, formatObjectId) {

  def store[T <: TaxHistory](taxHistory: T): Future[T] = {
    for {
      _ <- remove("nino" -> taxHistory.nino, "taxYear" -> taxHistory.taxYear)
      _ <- insert(taxHistory)
    } yield taxHistory
  }

  def fetch(nino: String, taxYear: String): Future[Option[TaxHistory]] = {
    find("nino" -> nino, "taxYear" -> taxYear) map(_.headOption)
  }
}
