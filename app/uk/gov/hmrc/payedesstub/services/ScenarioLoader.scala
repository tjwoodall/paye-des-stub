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

package uk.gov.hmrc.payedesstub.services

import javax.inject.Singleton
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.payedesstub.models.InvalidScenarioException

import scala.concurrent.Future
import scala.io.Source

@Singleton
class ScenarioLoader {

  private def pathForScenario(api: String, scenario: String) = {
    s"/public/scenarios/$api/$scenario.json"
  }

  def loadScenario[T: Reads](api: String, scenario: String): Future[T] = {
    val resource = getClass.getResourceAsStream(pathForScenario(api, scenario))
    if (resource == null) {
      Future.failed(new InvalidScenarioException(scenario))
    } else {
      Future.successful(Json.parse(resource).as[T])
    }
  }

  def loadScenarioRaw(api: String, scenario: String): Future[String] = {
    val resource = getClass.getResourceAsStream(pathForScenario(api, scenario))

    if (resource == null) {
      Future.failed(new InvalidScenarioException(scenario))
    } else {
      Future.successful(Source.fromInputStream(resource).mkString)
    }
  }

}