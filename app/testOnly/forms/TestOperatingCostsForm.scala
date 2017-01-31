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

package testOnly.forms

import models.OperatingCostsModel
import play.api.data.Form
import play.api.data.Forms._

object TestOperatingCostsForm {

  val testOperatingCostsForm = Form(
    mapping(
      "operatingCosts1stYear" -> utils.Validation.mandatoryMaxTenNumberNonZeroValidation("page.companyDetails.OperatingCosts.error.field.one"),
      "operatingCosts2ndYear" -> utils.Validation.mandatoryMaxTenNumberNonZeroValidation("page.companyDetails.OperatingCosts.error.field.two"),
      "operatingCosts3rdYear" -> utils.Validation.mandatoryMaxTenNumberNonZeroValidation("page.companyDetails.OperatingCosts.error.field.three"),
      "rAndDCosts1stYear" -> utils.Validation.mandatoryMaxTenNumberValidation("page.companyDetails.OperatingCosts.error.field.four"),
      "rAndDCosts2ndYear" -> utils.Validation.mandatoryMaxTenNumberValidation("page.companyDetails.OperatingCosts.error.field.five"),
      "rAndDCosts3rdYear" -> utils.Validation.mandatoryMaxTenNumberValidation("page.companyDetails.OperatingCosts.error.field.six"),
      "operatingCostsFirstYear" -> utils.Validation.fourDigitYearCheck,
      "operatingCostsSecondYear" -> utils.Validation.fourDigitYearCheck,
      "operatingCostsThirdYear" -> utils.Validation.fourDigitYearCheck
    )(OperatingCostsModel.apply)(OperatingCostsModel.unapply)
  )
}
