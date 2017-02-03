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

package forms

import models.{DateOfIncorporationModel, TradeStartDateModel}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import utils.Validation._

object TradeStartDateForm {
  val tradeStartDateForm = Form(
    mapping(
      "hasTradeStartDate" -> nonEmptyText,
      "tradeStartDay" -> optional(number),
      "tradeStartMonth" -> optional(number),
      "tradeStartYear" -> optional(number)
    )
    (TradeStartDateModel.apply)(TradeStartDateModel.unapply)
//      .verifying(Messages("validation.error.DateNotEntered"), fields =>
//        validateNonEmptyDateOptions(fields.tradeStartDay, fields.tradeStartMonth, fields.tradeStartYear))
//      .verifying(Messages("common.date.error.invalidDate"), fields =>
//        isValidDateOptions(fields.tradeStartDay, fields.tradeStartMonth, fields.tradeStartYear))
//      .verifying(Messages("validation.error.tradeStartDate.Future"), fields =>
//        dateNotInFutureOptions(fields.tradeStartDay, fields.tradeStartMonth, fields.tradeStartYear))
  //(TradeStartDateModel.apply)(TradeStartDateModel.unapply)
  )
}
