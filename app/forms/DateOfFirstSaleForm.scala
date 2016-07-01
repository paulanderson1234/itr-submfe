/*
 * Copyright 2016 HM Revenue & Customs
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

import common.Dates._
import utils.Validation._
import models.DateOfFirstSaleModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages

object DateOfFirstSaleForm {
  val dateOfFirstSaleForm = Form(
    mapping(
      "dateOfFirstSaleDay" -> number,
      "dateOfFirstSaleMonth" -> number,
      "dateOfFirstSaleYear" -> number
    )(DateOfFirstSaleModel.apply)(DateOfFirstSaleModel.unapply)
      .verifying(Messages("common.date.error.invalidDate"), fields =>
        isValidDate(fields.day, fields.month, fields.year))
      .verifying(Messages("validation.error.DateOfFirstSale.Future"), fields =>
        dateNotInFuture(fields.day, fields.month, fields.year))
  )
}
