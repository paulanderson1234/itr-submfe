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

import models.AnnualTurnoverCostsModel
import play.api.data.Forms._
import play.api.data.Form
import utils.Validation._

object TurnoverCostsForm {

  val turnoverCostsForm = Form(
    mapping(
      "amount1" -> mandatoryMaxTenNumberNonZeroValidation("page.companyDetails.TurnoverCosts.error.field.one"),
      "amount2" -> mandatoryMaxTenNumberNonZeroValidation("page.companyDetails.TurnoverCosts.error.field.two"),
      "amount3" -> mandatoryMaxTenNumberNonZeroValidation("page.companyDetails.TurnoverCosts.error.field.three"),
      "amount4" -> mandatoryMaxTenNumberNonZeroValidation("page.companyDetails.TurnoverCosts.error.field.four"),
      "amount5" -> mandatoryMaxTenNumberNonZeroValidation("page.companyDetails.TurnoverCosts.error.field.five"),
      "firstYear" -> fourDigitYearCheck,
      "secondYear" -> fourDigitYearCheck,
      "thirdYear" -> fourDigitYearCheck,
      "fourthYear" -> fourDigitYearCheck,
      "fifthYear" -> fourDigitYearCheck
    )(AnnualTurnoverCostsModel.apply)(AnnualTurnoverCostsModel.unapply)
  )
}
