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

package controllers

import config.FrontendGlobal.internalServerErrorTemplate
import auth.AuthorisedAndEnrolledForTAVC
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.hubPartials.ConfirmDeleteApplication
import models.submission.SchemeTypesModel
import play.api.i18n.Messages

import scala.concurrent.Future

object ConfirmDeleteApplicationController extends ConfirmDeleteApplicationController {
  override lazy val s4lConnector = S4LConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
}

trait ConfirmDeleteApplicationController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq()

  def show(): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(schemeTypesModel: Option[SchemeTypesModel]): Future[Result] = {
      if (schemeTypesModel.nonEmpty) {
        s4lConnector.fetchAndGetFormData[Boolean](KeystoreKeys.applicationInProgress).map {
          case Some(true) => Ok(ConfirmDeleteApplication(Messages("common.application.types.advanceAssurance"),
            ControllerHelpers.removeDescriptionFromTypes(schemeTypesModel)))
          case _ => Redirect(routes.ApplicationHubController.show())
        }
      }
      else Future.successful(InternalServerError(internalServerErrorTemplate))
    }

    (for {
      schemeTypesModel <- s4lConnector.fetchAndGetFormData[SchemeTypesModel](KeystoreKeys.selectedSchemes)
      route <- routeRequest(schemeTypesModel)
    } yield route) recover {
      case e: Exception => {
        Logger.warn(s"[ApplicationHubController][getApplicationModel] - Exception occurred: ${e.getMessage}")
        InternalServerError(internalServerErrorTemplate)
      }
    }
  }

  val delete = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.clearCache().map {
      case _ => Redirect(routes.ApplicationHubController.show())
    }
  }

}
