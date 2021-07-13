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

import org.mongodb.scala.model.Filters._
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.payedesstub.models._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxHistoryRepository @Inject()(mongo: MongoComponent)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[TaxHistory](
    mongoComponent = mongo,
    collectionName = "taxHistory",
    domainFormat   = formatTaxHistory,
    indexes = Seq.empty
  )
{

  def store (taxHistory: TaxHistory): Future[TaxHistory] = {
    collection.findOneAndReplace(and(equal("nino" , taxHistory.nino), equal("taxYear" , taxHistory.taxYear)),taxHistory).toFuture()
  }

  def fetch(nino: String, taxYear: String): Future[Option[TaxHistory]] = {
    collection.find(and(equal("nino" , nino), equal("taxYear" , taxYear))).headOption()
  }
}
