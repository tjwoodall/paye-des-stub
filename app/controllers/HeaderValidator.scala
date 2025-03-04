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

import models.ErrorAcceptHeaderInvalid
import play.api.http.HeaderNames.ACCEPT
import play.api.mvc.{ActionFilter, ControllerComponents, Request, Result, Results}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

trait HeaderValidator extends Results with ErrorConversion {

  def cc: ControllerComponents

  private val validateContentType: String => Boolean = ct => ct == "json"

  private val matchHeader: String => Option[Match] =
    new Regex("""^application/vnd\.hmrc\.(.*?)\+(.*)$""", "version", "contenttype") findFirstMatchIn _

  def acceptHeaderValidationRules(versions: String*): Option[String] => Boolean =
    _ flatMap (a =>
      matchHeader(a) map (res =>
        validateContentType(res.group("contenttype")) && versions.contains(res.group("version"))
      )
    ) getOrElse false

  private def validateAction(rules: Option[String] => Boolean): ActionFilter[Request] = new ActionFilter[Request] {

    def filter[T](input: Request[T]): Future[Option[Result]] = Future.successful {
      if (!rules(input.headers.get(ACCEPT))) {
        Some(ErrorAcceptHeaderInvalid)
      } else {
        None
      }
    }

    override protected def executionContext: ExecutionContext = cc.executionContext
  }

  def validateAcceptHeader(versions: String*): ActionFilter[Request] =
    validateAction(acceptHeaderValidationRules(versions*))
}
