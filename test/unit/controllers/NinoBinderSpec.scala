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

package unit.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.payedesstub.controllers.Binders

class NinoBinderSpec extends AnyWordSpecLike with Matchers with OptionValues {

  "Nino binding" should {

    "return a NINO object when the NINO is valid" in {
      val validNinoString = "AA000000A"
      val expectedNino = Nino(validNinoString)
      val result: Either[String, Nino] = Binders.ninoBinder.bind("x", validNinoString)
      result shouldBe Right(expectedNino)
    }

    "indicate an error when the UTR is invalid" in {
      val result: Either[String, Nino] = Binders.ninoBinder.bind("x", "123")
      result shouldBe Left("ERROR_NINO_INVALID")
    }
  }
}


