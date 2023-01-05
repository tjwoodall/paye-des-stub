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

import models.TaxYear
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TaxYearParsingSpec extends AnyWordSpec with Matchers {

  "a valid TaxYear" should {
    "be transformed and startYr should be 2014" in {
      TaxYear("2014-15").startYr shouldBe "2014"
    }
  }

  "a valid TaxYear" should {
    "be transformed and startYr should be 2015" in {
      TaxYear("2015-16").startYr shouldBe "2015"
    }
  }

}
