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

package repositories

import org.mongodb.scala.model.Filters._
import javax.inject.{Inject, Singleton}
import models._
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.MongoComponent
import org.mongodb.scala.{ObservableFuture, SingleObservableFuture}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndividualBenefitsRepository @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[IndividualBenefits](
      mongoComponent = mongo,
      collectionName = "individualBenefits",
      domainFormat = formatIndividualBenefits,
      indexes = Seq.empty
    ) {

  def store[T <: IndividualBenefits](individualBenefits: T): Future[T] =
    collection.insertOne(individualBenefits).toFuture().map(_ => individualBenefits)

  def fetch(utr: String, taxYear: String): Future[Option[IndividualBenefits]] =
    collection.find(and(equal("utr", utr), equal("taxYear", taxYear))).headOption()
}
