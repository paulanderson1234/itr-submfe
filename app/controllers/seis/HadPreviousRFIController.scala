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
import forms.HadPreviousRFIForm._
import models.HadPreviousRFIModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.seis.previousInvestment.HadPreviousRFI

import scala.concurrent.Future

object HadPreviousRFIController extends HadPreviousRFIController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait HadPreviousRFIController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with PreviousSchemesHelper {

  override val acceptedFlows = Seq(Seq(SEIS))


  //
  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI).map {
        case Some(data) => Ok(HadPreviousRFI(hadPreviousRFIForm.fill(data)))
        case None => Ok(HadPreviousRFI(hadPreviousRFIForm))
      }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.saveFormData(KeystoreKeys.backLinkHadRFI, routes.HadPreviousRFIController.show().url)
      hadPreviousRFIForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(HadPreviousRFI(formWithErrors)))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.hadPreviousRFI, validFormData)
          Future.successful(Redirect(routes.HadOtherInvestmentsController.show()))
        }
      )
    }
  }
}