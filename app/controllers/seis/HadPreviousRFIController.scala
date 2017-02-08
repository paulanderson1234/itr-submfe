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

import auth.AuthorisedAndEnrolledForTAVC
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.PreviousSchemesHelper
import controllers.featureSwitch.SEISFeatureSwitch
import forms.HadPreviousRFIForm._
import models.HadPreviousRFIModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.seis.previousInvestment.HadPreviousRFI

import scala.concurrent.Future

object HadPreviousRFIController extends HadPreviousRFIController{
  val s4lConnector: S4LConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait HadPreviousRFIController extends FrontendController with AuthorisedAndEnrolledForTAVC with SEISFeatureSwitch with PreviousSchemesHelper {

  val s4lConnector: S4LConnector
  //
  val show = seisFeatureSwitch {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI).map {
        case Some(data) => Ok(HadPreviousRFI(hadPreviousRFIForm.fill(data)))
        case None => Ok(HadPreviousRFI(hadPreviousRFIForm))
      }
    }
  }


  val submit = seisFeatureSwitch {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      hadPreviousRFIForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(HadPreviousRFI(formWithErrors)))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.hadPreviousRFI, validFormData)
          validFormData.hadPreviousRFI match {

            case Constants.StandardRadioButtonYesValue => {
              getAllInvestmentFromKeystore(s4lConnector).flatMap {
                previousSchemes =>
                  if (previousSchemes.nonEmpty) {
                    s4lConnector.saveFormData(KeystoreKeys.backLinkReviewPreviousSchemes, routes.HadPreviousRFIController.show().url)
                    Future.successful(Redirect(routes.ReviewPreviousSchemesController.show()))
                  }
                  else {
                    s4lConnector.saveFormData(KeystoreKeys.backLinkPreviousScheme, routes.HadPreviousRFIController.show().url)
                    Future.successful(Redirect(routes.PreviousSchemeController.show()))
                  }
              }
            }
            case Constants.StandardRadioButtonNoValue => {
              s4lConnector.saveFormData(KeystoreKeys.backLinkProposedInvestment, routes.HadPreviousRFIController.show().url)
              clearPreviousInvestments(s4lConnector)
              Future.successful(Redirect(routes.ProposedInvestmentController.show()))
            }

          }
        }
      )
    }
  }
}