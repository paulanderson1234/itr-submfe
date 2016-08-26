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

  def validateInput(date: Option[DateOfIncorporationModel], rAndCost: Option[OperatingCostsModel],
              percentageStaffMasters: Option[PercentageStaffWithMastersModel]) : Future[Boolean] = (date, rAndCost, percentageStaffMasters) match {

    case (Some(date), Some(rAndCost), Some(percentageStaffMasters)) => {
      Future.successful(Some(true))
    }
    case _ => Future.successful(false)
  }

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

  def getKI(implicit hc: HeaderCarrier) : Future[Future[Option[Boolean]]] = {

    def checkKI(date: Option[DateOfIncorporationModel], rAndCost: Option[OperatingCostsModel],
                percentageStaffMasters: Option[PercentageStaffWithMastersModel], tenYearPlan: Option[TenYearPlanModel]) : Future[Option[Boolean]] = {

      validateInput(date, rAndCost, percentageStaffMasters).map{
        case true => {
          tenYearPlan match {
            case (Some(tenYearPlan)) =>
              if (checkDateAndCosts(date.get, rAndCost.get) && (tenYearPlan.hasTenYearPlan == Constants.StandardRadioButtonYesValue)
                || percentageStaffMasters.get.staffWithMasters == Constants.StandardRadioButtonYesValue) Some(true)
              else
                Some(false)

            case (None) => if (checkDateAndCosts(date.get, rAndCost.get) &&
              percentageStaffMasters.get.staffWithMasters == Constants.StandardRadioButtonYesValue) Some(true)
            else
              Some(false)
          }
        }
        case false => None
      }
    }

    for {
      date <- keyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
      masters <- keyStoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters)
      tenYearPlan <- keyStoreConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan)
      operatingCosts <- keyStoreConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts)
      isKI <- Future.successful(checkKI(date, operatingCosts, masters, tenYearPlan))
    } yield isKI
  }
}
