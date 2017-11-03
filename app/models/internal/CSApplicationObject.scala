/*
 * Copyright 2017 HM Revenue & Customs
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

package models.internal

import play.api.libs.json.Json

case class CSApplicationModel(inProgress: Boolean, schemeType: Option[String]) {
  private val isValidInProgress = if (inProgress) schemeType.isDefined else true
  private val isValidNotInProgress = if (!inProgress) schemeType.isEmpty else true
  require(isValidInProgress && isValidNotInProgress)
}

object CSApplicationModel{
  implicit val formats = Json.format[CSApplicationModel]
}
