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

package unit.services

import unit.util.ResourceLoader._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import models.{IndividualTaxResponse, InvalidScenarioException}
import services.ScenarioLoader

import scala.concurrent.ExecutionContext.Implicits.global

class ScenarioLoaderSpec extends AnyWordSpec with Matchers {

  private val scenarioLoader: ScenarioLoader = new ScenarioLoader

  private val happyPath: String = loadResource("/public/scenarios/individual-tax/HAPPY_PATH_1.json")

  "ScenarioLoader" when {
    "loadScenario" should {
      "return Happy Path when valid scenario is supplied" in {
        val result = scenarioLoader.loadScenario[IndividualTaxResponse]("individual-tax", "HAPPY_PATH_1")
        result.map(_ shouldBe happyPath)
      }

      "return InvalidScenarioException when invalid scenario is supplied" in {
        val result = scenarioLoader.loadScenario[IndividualTaxResponse]("individual-tax", "HAPPY")
        result.map(_ shouldBe new InvalidScenarioException("HAPPY"))
      }
    }
  }
}
