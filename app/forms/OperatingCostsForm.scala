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

import models.OperatingCostsModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import utils.Transfomers._
import utils.Validation._

object OperatingCostsForm {

  val maxAllowableAmount: Int = 999999999
  val minAllowableAmount: Int = 1

  //    "operatingCosts1stYear" -> text
  //        .verifying(Messages("validation.common.error.fieldRequired"), mandatoryCheck)
  //        .verifying(Messages("page.companyDetails.OperatingCosts.amount.invalidAmount"), integerCheck)
  //        .verifying(Messages("page.companyDetails.OperatingCosts.amount.OutOfRange"), minIntCheckString(minAllowableAmount))
  //        .verifying(Messages("page.companyDetails.OperatingCosts.amount.OutOfRange"), maxIntCheckString(maxAllowableAmount)),

  val operatingCostsForm = Form(
    mapping(
      "operatingCosts1stYear" -> utils.Validation.mandatoryMaxTenNumberNonZeroValidation("page.companyDetails.OperatingCosts.error.field.one"),
      "operatingCosts2ndYear" -> utils.Validation.mandatoryMaxTenNumberNonZeroValidation("page.companyDetails.OperatingCosts.error.field.two"),
      "operatingCosts3rdYear" -> utils.Validation.mandatoryMaxTenNumberNonZeroValidation("page.companyDetails.OperatingCosts.error.field.three"),
      "rAndDCosts1stYear" -> utils.Validation.mandatoryMaxTenNumberValidation("page.companyDetails.OperatingCosts.error.field.four"),
      "rAndDCosts2ndYear" -> utils.Validation.mandatoryMaxTenNumberValidation("page.companyDetails.OperatingCosts.error.field.five"),
      "rAndDCosts3rdYear" -> utils.Validation.mandatoryMaxTenNumberValidation("page.companyDetails.OperatingCosts.error.field.six")
    )(OperatingCostsModel.apply)(OperatingCostsModel.unapply)
  )
}
