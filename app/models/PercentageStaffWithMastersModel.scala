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

package models

import common.Constants
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

case class PercentageStaffWithMastersModel (staffWithMasters: String)

object PercentageStaffWithMastersModel {
  implicit val format = Json.format[PercentageStaffWithMastersModel]
  implicit val writes = Json.writes[PercentageStaffWithMastersModel]

  def staffWithMastersToString(matcher: String): String = {
    matcher match {
      case Constants.StandardRadioButtonYesValue => Messages("page.percentageStaffWithMasters.more")
      case Constants.StandardRadioButtonNoValue => Messages("page.percentageStaffWithMasters.less")
      case _ => Messages("common.notAvailable")
    }
  }
}
