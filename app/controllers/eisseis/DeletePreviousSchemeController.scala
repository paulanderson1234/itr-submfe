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

import config.FrontendGlobal.internalServerErrorTemplate
import auth.{AuthorisedAndEnrolledForTAVC, EIS, SEIS, VCT}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{EisSeisHelper, PreviousSchemesHelper}
import controllers.predicates.FeatureSwitch
import forms.PreviousSchemeDeleteForm._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.eisseis.previousInvestment.DeletePreviousScheme

import scala.concurrent.Future

object DeletePreviousSchemeController extends DeletePreviousSchemeController {
  override lazy val s4lConnector = S4LConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
}

trait DeletePreviousSchemeController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch{

  override val acceptedFlows = Seq(Seq(EIS, SEIS, VCT), Seq(SEIS, VCT), Seq(EIS, SEIS))

  def show(previousSchemeId: Int): Action[AnyContent] = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      PreviousSchemesHelper.getExistingInvestmentFromKeystore(s4lConnector, previousSchemeId).flatMap {
        scheme => Future.successful(Ok(DeletePreviousScheme(scheme.get)))
      }.recover {
        case e: Exception => {
          Logger.warn(s"[DeletePreviousSchemeController][show] - Exception retrieving scheme id: ${e.getMessage}")
          InternalServerError(internalServerErrorTemplate)
        }
      }
    }
  }

  def submit(): Action[AnyContent] = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      previousSchemeDeleteForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(InternalServerError(internalServerErrorTemplate))
        },
        validFormData => {
          for {
            delete <- PreviousSchemesHelper.removeKeystorePreviousInvestment(s4lConnector, validFormData.previousSchemeId.toInt)
            update <- EisSeisHelper.updateIneligiblePreviousSchemeTypeCondition(s4lConnector)
            route <- Future.successful(Redirect(routes.ReviewPreviousSchemesController.show()))
          } yield route
        }
      )
    }
  }

}
