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

import models.ErrorResponse
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status.NOT_ACCEPTABLE
import play.api.libs.json.Json
import play.api.mvc.Results.Status

class ErrorConversionSpec extends AnyWordSpecLike with Matchers with ErrorConversion {

  "ErrorConversion" should {
    "convert errorResponse to the correct error JSON response" in {
      val errorResponse =
        ErrorResponse(NOT_ACCEPTABLE, "ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")

      toResult(errorResponse) shouldBe Status(NOT_ACCEPTABLE)(Json.toJson(errorResponse))
    }
  }
}
