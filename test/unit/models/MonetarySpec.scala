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

package unit.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json, OFormat}
import models.IndividualResponse.doubleWrite

class MonetarySpec extends AnyWordSpec with Matchers {

  case class Test(d: Double)

  object Test {
    implicit val format: OFormat[Test] = Json.format[Test]
  }

  private val json: String => JsValue = (d: String) => Json.parse(s"""{"d":$d}""".stripMargin)

  private val m: Map[Double, String] = Map(
    100d     -> "100",
    100.4d   -> "100.4",
    100.9d   -> "100.9",
    200.24d  -> "200.24",
    200.44d  -> "200.44",
    200.49d  -> "200.49",
    200.99d  -> "200.99",
    200.49d  -> "200.49",
    300.345d -> "300.34",
    300.346d -> "300.34",
    300.389d -> "300.38",
    300.999d -> "300.99",
    300.991d -> "300.99"
  )

  "Monetary" should {
    m.foreach { case (d, s) =>
      s"be translated from value $d to $s" in {
        val generatedJson = Json.toJson(Test(d))
        val expectedJson  = json(s)
        generatedJson shouldBe expectedJson
      }
    }
  }
}
