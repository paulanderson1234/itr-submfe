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

object OperatingCostsForm {
  val operatingCostsForm = Form(
    mapping(
      "operatingCosts1stYear" -> utils.Validation.mandatoryNumberCheck("Operating Costs 1st Year"),
      "operatingCosts2ndYear" -> utils.Validation.mandatoryNumberCheck("Operating Costs 2nd Year"),
      "operatingCosts3rdYear" -> utils.Validation.mandatoryNumberCheck("Operating Costs 3rd Year"),
      "rAndDCosts1stYear" -> utils.Validation.mandatoryNumberCheck("Research and Development Costs 1st Year"),
      "rAndDCosts2ndYear" -> utils.Validation.mandatoryNumberCheck("Research and Development Costs 2nd Year"),
      "rAndDCosts3rdYear" -> utils.Validation.mandatoryNumberCheck("Research and Development Costs 3rd Year")
    )(OperatingCostsModel.apply)(OperatingCostsModel.unapply)
  )
}
