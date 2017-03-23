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

import auth.{AuthorisedAndEnrolledForTAVC, EIS, SEIS, VCT}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{ControllerHelpers, PreviousSchemesHelper}
import controllers.predicates.FeatureSwitch
import forms.HadPreviousRFIForm._
import models.HadPreviousRFIModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import views.html.eisseis.previousInvestment

object HadPreviousRFIController extends HadPreviousRFIController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait HadPreviousRFIController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with PreviousSchemesHelper {

  override val acceptedFlows = Seq(Seq(EIS,SEIS,VCT),Seq(SEIS,VCT), Seq(EIS,SEIS))

  val show = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      def routeRequest(backUrl: Option[String]) = {
        if (backUrl.isDefined) {
          s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI).map {
            case Some(data) => Ok(previousInvestment.HadPreviousRFI(hadPreviousRFIForm.fill(data), backUrl.getOrElse("")))
            case None => Ok(previousInvestment.HadPreviousRFI(hadPreviousRFIForm, backUrl.getOrElse("")))
          }
        }
        else Future.successful(Redirect(routes.CommercialSaleController.show()))
      }
      for {
        link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSubsidiaries, s4lConnector)
        route <- routeRequest(link)
      } yield route
    }
  }

  val submit = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      hadPreviousRFIForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSubsidiaries, s4lConnector).flatMap {
            case Some(data) => Future.successful(BadRequest(previousInvestment.HadPreviousRFI(formWithErrors, data)))
            case None => Future.successful(Redirect(routes.CommercialSaleController.show()))
          }
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.hadPreviousRFI, validFormData)
          s4lConnector.saveFormData(KeystoreKeys.backLinkHadRFI, routes.HadPreviousRFIController.show().url)
          Future.successful(Redirect(routes.HadOtherInvestmentsController.show()))

        }
      )
    }
  }

}
