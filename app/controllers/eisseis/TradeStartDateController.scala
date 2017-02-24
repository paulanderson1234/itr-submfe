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

package controllers.eisseis

import auth._
import common.{Constants, KeystoreKeys}
import config.FrontendGlobal._
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.Helpers.EisSeisHelper
import controllers.predicates.FeatureSwitch
import forms.TradeStartDateForm._
import models.TradeStartDateModel
import play.Logger
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eisseis.companyDetails.TradeStartDate
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.Result

import scala.concurrent.Future

object TradeStartDateController extends TradeStartDateController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val submissionConnector = SubmissionConnector
}

trait TradeStartDateController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(ALLFLOWS))

  val submissionConnector: SubmissionConnector

  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[TradeStartDateModel](KeystoreKeys.tradeStartDate).map {
        case Some(data) => Ok(TradeStartDate(tradeStartDateForm.fill(data)))
        case None => Ok(TradeStartDate(tradeStartDateForm))
      }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      tradeStartDateForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(TradeStartDate(formWithErrors)))
        },
        validFormData => {
          validFormData.hasTradeStartDate match {
            case Constants.StandardRadioButtonYesValue =>
              //s4lConnector.saveFormData(KeystoreKeys.tradeStartDate, validFormData)
              submissionConnector.validateTradeStartDateCondition(validFormData.tradeStartDay.get,
                validFormData.tradeStartMonth.get, validFormData.tradeStartYear.get).flatMap {
                case Some(validated) => if (validated) {
                  s4lConnector.saveFormData(KeystoreKeys.tradeStartDate, validFormData)
                  EisSeisHelper.setStartDateCondition(s4lConnector, tradeStartConditionIneligible = false)
                  Future.successful(Redirect(routes.CommercialSaleController.show()))
                } else EisSeisHelper.shouldDisplayTradeStartDateError(s4lConnector, validFormData).map {
                  // update start date condition only after checking if already seis ineligible
                  // only show seis error if not already ineligible (as already shown it before otherwise)
                  // however show errro if the data has changed
                  case shouldDisplayError if shouldDisplayError =>
                    s4lConnector.saveFormData(KeystoreKeys.tradeStartDate, validFormData)
                    EisSeisHelper.setStartDateCondition(s4lConnector, tradeStartConditionIneligible = true)
                    Redirect(routes.TradeStartDateErrorController.show())
                  case _ =>
                    s4lConnector.saveFormData(KeystoreKeys.tradeStartDate, validFormData)
                    EisSeisHelper.setStartDateCondition(s4lConnector, tradeStartConditionIneligible = true)
                    Redirect(routes.CommercialSaleController.show())
                }
                case _ =>
                  s4lConnector.saveFormData(KeystoreKeys.tradeStartDate, validFormData)
                  Logger.warn(s"[TradeStartDateController][submit] - Call to validate trade start date in backend failed")
                  Future.successful(InternalServerError(internalServerErrorTemplate))
              }.recover {
                case e: Exception =>
                  s4lConnector.saveFormData(KeystoreKeys.tradeStartDate, validFormData)
                  Logger.warn(s"[TradeStartDateController][submit] - Exception: ${e.getMessage}")
                  InternalServerError(internalServerErrorTemplate)
              }
            case Constants.StandardRadioButtonNoValue =>
              s4lConnector.saveFormData(KeystoreKeys.tradeStartDate, validFormData)
              // if there is no start date chosen. remove any existing ineligibility for the start date condition
              EisSeisHelper.setStartDateCondition(s4lConnector, tradeStartConditionIneligible = false)
              Future.successful(Redirect(routes.CommercialSaleController.show()))
          }
        }
      )
    }
  }
}