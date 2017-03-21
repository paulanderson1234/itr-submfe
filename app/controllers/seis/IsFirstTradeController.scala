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

package controllers.seis

import auth.{AuthorisedAndEnrolledForTAVC, SEIS}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.PreviousSchemesHelper
import controllers.predicates.FeatureSwitch
import forms.IsFirstTradeForm._
import models.IsFirstTradeModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.seis.companyDetails.IsFirstTrade
import views.html.seis.companyDetails.NotFirstTradeError

import scala.concurrent.Future

object IsFirstTradeController extends IsFirstTradeController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait IsFirstTradeController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with PreviousSchemesHelper {

  override val acceptedFlows = Seq(Seq(SEIS))


  //
  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      s4lConnector.fetchAndGetFormData[IsFirstTradeModel](KeystoreKeys.isFirstTrade).map {
        case Some(data) => Ok(IsFirstTrade(isFirstTradeForm.fill(data)))
        case None => Ok(IsFirstTrade(isFirstTradeForm))
      }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      isFirstTradeForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(IsFirstTrade(formWithErrors)))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.isFirstTrade, validFormData)
          validFormData.isFirstTrade match {

            case Constants.StandardRadioButtonYesValue => {
        // to navigate to usedInvestmentSchemeBefore for SEIS only flow
              Future.successful(Redirect(routes.HadPreviousRFIController.show()))
            }
            case Constants.StandardRadioButtonNoValue => {
         // to navigate to errorNotFirstTrade for SEIS only flow
              Future.successful(Redirect(routes.NotFirstTradeController.show()))
            }
          }
        }
      )
    }
  }
}
