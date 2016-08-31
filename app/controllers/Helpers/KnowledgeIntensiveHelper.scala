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

package controllers.Helpers

import common.Constants
import models.{DateOfIncorporationModel, OperatingCostsModel, PercentageStaffWithMastersModel, TenYearPlanModel}
import utils.Validation


object KnowledgeIntensiveHelper extends KnowledgeIntensiveHelper {

}

trait KnowledgeIntensiveHelper {

  def validateInput(date: Option[DateOfIncorporationModel], percentageStaffMasters: Option[PercentageStaffWithMastersModel]):
                    Boolean = (date, percentageStaffMasters) match {

    case (Some(date), Some(percentageStaffMasters)) => true
    case _ => false
  }

  def checkRAndDCosts(rAndDCost: OperatingCostsModel): Boolean =  {

    def findCosts(operatingCosts: scala.Double, divideBy: Int): Double = {
      val amount = (operatingCosts / 100) * divideBy
      amount
    }

    if ((rAndDCost.rAndDCosts1stYear.toDouble >= findCosts(rAndDCost.operatingCosts1stYear.toDouble, Constants.KI10Percent) &&
      rAndDCost.rAndDCosts2ndYear.toDouble >= findCosts(rAndDCost.operatingCosts2ndYear.toDouble, Constants.KI10Percent) &&
      rAndDCost.rAndDCosts3rdYear.toDouble >= findCosts(rAndDCost.operatingCosts3rdYear.toDouble, Constants.KI10Percent)) ||
      (rAndDCost.rAndDCosts1stYear.toDouble >= findCosts(rAndDCost.operatingCosts1stYear.toDouble, Constants.KI15Percent) ||
        rAndDCost.rAndDCosts2ndYear.toDouble >= findCosts(rAndDCost.operatingCosts2ndYear.toDouble, Constants.KI15Percent) ||
        rAndDCost.rAndDCosts3rdYear.toDouble >= findCosts(rAndDCost.operatingCosts3rdYear.toDouble, Constants.KI15Percent)))
      true
    else
      false
  }

  def checkKI(date: DateOfIncorporationModel, percentageStaffMasters: PercentageStaffWithMastersModel,
              tenYearPlan : Boolean, tenYearPlanModel : Option[TenYearPlanModel]) : Boolean = {

    if(tenYearPlan)
      if (Validation.dateAfterIncorporationRule(date.day.get, date.month.get, date.year.get) &&
        tenYearPlanModel.get.hasTenYearPlan == Constants.StandardRadioButtonYesValue
        || percentageStaffMasters.staffWithMasters == Constants.StandardRadioButtonYesValue)
        true
      else
        false
    else
    if(Validation.dateAfterIncorporationRule(date.day.get, date.month.get, date.year.get) &&
      percentageStaffMasters.staffWithMasters == Constants.StandardRadioButtonYesValue)
      true
    else
      false
  }
}
