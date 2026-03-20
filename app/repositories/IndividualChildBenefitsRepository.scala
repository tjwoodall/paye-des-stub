/*
 * Copyright 2026 HM Revenue & Customs
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

import models.*
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Filters.*
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndividualChildBenefitsRepository @Inject()(mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[IndividualChildBenefits](
      mongoComponent = mongo,
      collectionName = "individualChildBenefits",
      domainFormat = formatIndividualChildBenefits,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("utr", "taxYear"),
          IndexOptions().name("individual-child-benefits-utr-taxYear").unique(true)
        )
      )
    ) {

  private def filter(utr: String, taxYear: String) =
    Filters.and(
      Filters.equal("utr", utr),
      Filters.equal("taxYear", taxYear)
    )

  def store[T <: IndividualChildBenefits](individualChildBenefits: T): Future[T] =
    collection
      .replaceOne(
        filter = filter(individualChildBenefits.utr, individualChildBenefits.taxYear),
        replacement = individualChildBenefits,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => individualChildBenefits)

  def fetch(utr: String, taxYear: String): Future[Option[IndividualChildBenefits]] =
    collection.find(filter(utr, taxYear)).headOption()
}
