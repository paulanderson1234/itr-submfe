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

package controllers.hubGuidance

import auth.AuthorisedAndEnrolledForTAVC
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.eis
import models.submission.SchemeTypesModel
import play.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.hubGuidance.HubGuidanceFeedback

import scala.concurrent.Future

object HubGuidanceFeedbackController extends HubGuidanceFeedbackController
{
  override lazy val s4lConnector: S4LConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait HubGuidanceFeedbackController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq()

  val show = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      Future.successful(Ok(HubGuidanceFeedback()))
  }

  val submit = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      if(applicationConfig.eisseisFlowEnabled) {
        Future.successful(Redirect(controllers.schemeSelection.routes.SchemeSelectionController.show()))
      } else if(applicationConfig.seisFlowEnabled) {
        Future.successful(Redirect(controllers.schemeSelection.routes.SingleSchemeSelectionController.show()))
      }else {
        (for {
          saveApplication <- s4lConnector.saveFormData(KeystoreKeys.applicationInProgress, true)
          saveSchemes <- s4lConnector.saveFormData(KeystoreKeys.selectedSchemes, SchemeTypesModel(eis = true))
        } yield (saveApplication, saveSchemes)).map {
          result => Redirect(eis.routes.NatureOfBusinessController.show())
        }.recover {
          case e: Exception => Logger.warn(s"[HubGuidanceFeedbackController][newApplication] Exception when calling saveFormData: ${e.getMessage}")
            Redirect(eis.routes.NatureOfBusinessController.show())
        }
      }
  }
}