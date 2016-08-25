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

import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
import models._
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.Validation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object KnowledgeIntensiveHelper extends KnowledgeIntensiveHelper {
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait KnowledgeIntensiveHelper {

  val keyStoreConnector: KeystoreConnector

  def checkRAndDCosts(rAndDCost: OperatingCostsModel) : Boolean = {

    def findCosts(operatingCosts: scala.Double, divideBy: String) : Double = {
      val amount = (operatingCosts / 100) * divideBy.toInt
      amount
    }

    if((rAndDCost.rAndDCosts1stYear.toDouble > findCosts(rAndDCost.operatingCosts1stYear.toDouble, "10") &&
      rAndDCost.rAndDCosts2ndYear.toDouble > findCosts(rAndDCost.operatingCosts2ndYear.toDouble, "10") &&
      rAndDCost.rAndDCosts3rdYear.toDouble > findCosts(rAndDCost.operatingCosts3rdYear.toDouble, "10")) ||
      (rAndDCost.rAndDCosts1stYear.toDouble > findCosts(rAndDCost.operatingCosts1stYear.toDouble, "15") ||
        rAndDCost.rAndDCosts2ndYear.toDouble > findCosts(rAndDCost.operatingCosts2ndYear.toDouble, "15") ||
        rAndDCost.rAndDCosts3rdYear.toDouble > findCosts(rAndDCost.operatingCosts3rdYear.toDouble, "15")))
      true
    else
      false
  }

  def checkDateAndCosts(date: DateOfIncorporationModel, rAndCost: OperatingCostsModel) : Boolean = {
    if(Validation.dateAfterIncorporationRule(date.day.get, date.month.get, date.year.get) && checkRAndDCosts(rAndCost))
      true
    else false
  }

  def getKI(implicit hc: HeaderCarrier) : Future[Boolean] = {

    def checkKI(kiFlag: IsKnowledgeIntensiveModel, date: DateOfIncorporationModel, rAndCost: OperatingCostsModel,
                percentageStaffMasters: PercentageStaffWithMastersModel, tenYearPlan: Option[TenYearPlanModel]) : Boolean = {

      if(kiFlag.isKnowledgeIntensive == Constants.StandardRadioButtonYesValue) {
        calculateKI(date, rAndCost, percentageStaffMasters, tenYearPlan)
      }
      else false
    }

    def calculateKI(date: DateOfIncorporationModel, rAndCost: OperatingCostsModel, percentageStaffMasters: PercentageStaffWithMastersModel,
                    tenYearPlan: Option[TenYearPlanModel]) : Boolean = tenYearPlan match {

      case (Some(tenYearPlan)) =>
        if(checkDateAndCosts(date, rAndCost) && (tenYearPlan.hasTenYearPlan == Constants.StandardRadioButtonYesValue)
          || percentageStaffMasters.staffWithMasters == Constants.StandardRadioButtonYesValue) true
         else
          false

      case (None) => if(checkDateAndCosts(date, rAndCost) && percentageStaffMasters.staffWithMasters == Constants.StandardRadioButtonYesValue) true
      else
        false
    }

    for {
      kiFlag <- keyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](KeystoreKeys.isKnowledgeIntensive)
      date <- keyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
      masters <- keyStoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters)
      tenYearPlan <- keyStoreConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan)
      operatingCosts <- keyStoreConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts)
      isKI <- Future.successful(checkKI(kiFlag.get, date.get, operatingCosts.get, masters.get, tenYearPlan))
    } yield isKI
  }
}
