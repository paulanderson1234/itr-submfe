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

import auth.{AuthorisedAndEnrolledForTAVC,SEIS, EIS, VCT}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import forms.UsedInvestmentReasonBeforeForm._
import models.UsedInvestmentReasonBeforeModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import views.html.eisseis.investment.UsedInvestmentReasonBefore

object UsedInvestmentReasonBeforeController extends UsedInvestmentReasonBeforeController {
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val s4lConnector = S4LConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait UsedInvestmentReasonBeforeController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(EIS,SEIS,VCT),Seq(SEIS,VCT), Seq(EIS,SEIS))

  val show = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](KeystoreKeys.usedInvestmentReasonBefore).map {
        case Some(data) => Ok(UsedInvestmentReasonBefore(usedInvestmentReasonBeforeForm.fill(data)))
        case None => Ok(UsedInvestmentReasonBefore(usedInvestmentReasonBeforeForm))
      }
    }
  }

  val submit = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit userr => implicit request =>
      usedInvestmentReasonBeforeForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(UsedInvestmentReasonBefore(formWithErrors)))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.usedInvestmentReasonBefore, validFormData)
          validFormData.usedInvestmentReasonBefore match {
            case Constants.StandardRadioButtonYesValue => {
              Future.successful(Redirect(routes.PreviousBeforeDOFCSController.show()))
            }
            case Constants.StandardRadioButtonNoValue => {
              s4lConnector.saveFormData(KeystoreKeys.backLinkNewGeoMarket,
                routes.UsedInvestmentReasonBeforeController.show().url)
              Future.successful(Redirect(routes.NewGeographicalMarketController.show()))
            }
          }
        }
      )
    }
  }

}
