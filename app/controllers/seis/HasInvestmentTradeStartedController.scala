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
import controllers.predicates.FeatureSwitch
import forms.HasInvestmentTradeStartedForm._
import models.HasInvestmentTradeStartedModel
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.companyDetails.HasInvestmentTradeStarted

import scala.concurrent.Future

object HasInvestmentTradeStartedController extends HasInvestmentTradeStartedController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}


trait HasInvestmentTradeStartedController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))


  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async {
      implicit user =>
        implicit request =>

          s4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](KeystoreKeys.hasInvestmentTradeStarted).map {
            case Some(data) => Ok(HasInvestmentTradeStarted(hasInvestmentTradeStartedForm.fill(data)))
            case None => Ok(HasInvestmentTradeStarted(hasInvestmentTradeStartedForm))
          }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      hasInvestmentTradeStartedForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(HasInvestmentTradeStarted(formWithErrors)))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.hasInvestmentTradeStarted, validFormData)
          validFormData.hasInvestmentTradeStarted match {
            case Constants.StandardRadioButtonYesValue => {
              Future.successful(Redirect(controllers.seis.routes.HasInvestmentTradeStartedController.show()))
            }
            case Constants.StandardRadioButtonNoValue => {
              Future.successful(Redirect(controllers.seis.routes.HasInvestmentTradeStartedController.show()))
            }
          }
        }
      )
    }
  }

}