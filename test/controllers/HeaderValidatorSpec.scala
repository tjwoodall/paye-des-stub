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

package controllers

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.mvc.ControllerComponents
import play.api.test.Helpers.stubControllerComponents

class HeaderValidatorSpec extends AnyWordSpecLike with Matchers with OptionValues with HeaderValidator {

  val cc: ControllerComponents = stubControllerComponents()

  "acceptHeaderValidationRules" should {
    "return false when the header value is missing" in {
      acceptHeaderValidationRules("1.0")(None) shouldBe false
    }

    "return true when the version and the content type in header value is well formatted" in {
      acceptHeaderValidationRules("1.0")(Some("application/vnd.hmrc.1.0+json")) shouldBe true
    }

    "return false when the content type in header value is missing" in {
      acceptHeaderValidationRules("1.0")(Some("application/vnd.hmrc.1.0")) shouldBe false
    }

    "return false when the content type in header value is not well formatted" in {
      acceptHeaderValidationRules("1.0")(Some("application/vnd.hmrc.v1+json")) shouldBe false
    }

    "return false when the content type in header value is not valid" in {
      acceptHeaderValidationRules("1.0")(Some("application/vnd.hmrc.notvalid+XML")) shouldBe false
    }

    "return false when the version in header value is not valid" in {
      acceptHeaderValidationRules("1.0")(Some("application/vnd.hmrc.notvalid+json")) shouldBe false
    }

    "return false when the version in header value is not in range" in {
      acceptHeaderValidationRules("0.9", "1.0", "1.2")(Some("application/vnd.hmrc.1.3+json")) shouldBe false
    }

    "return true when the version is in range and the content type in header value is well formatted" in {
      acceptHeaderValidationRules("0.9", "1.0", "1.2.Special")(
        Some("application/vnd.hmrc.1.2.Special+json")
      ) shouldBe true
    }
  }
}
