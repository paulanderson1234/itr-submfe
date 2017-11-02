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
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{ComplianceStatementConnector, EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.hubPartials.{ConfirmDeleteApplication, ConfirmDeleteCSApplication}
import models.submission.SchemeTypesModel
import play.api.i18n.Messages

import scala.concurrent.Future

object ConfirmDeleteCSApplicationController extends ConfirmDeleteCSApplicationController {
  override lazy val s4lConnector = S4LConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val complianceStatementConnector = ComplianceStatementConnector

}

trait ConfirmDeleteCSApplicationController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq()
  val complianceStatementConnector: ComplianceStatementConnector

  def show(): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
      complianceStatementConnector.getComplianceStatementApplication().map {
        csApplication => {
          if(csApplication.inProgress)  Ok(ConfirmDeleteCSApplication(Messages("common.application.types.complianceStatement"),
            Messages("page.deleteApplication.hub.compliance.statement.type", csApplication.schemeType.get match {
              case Constants.schemeTypeEis => Constants.PageInvestmentSchemeEisValue
              case Constants.schemeTypeSeis => Constants.PageInvestmentSchemeSeisValue
            })))
          else Redirect(routes.ApplicationHubController.show())
        }
      }
  }

  val delete = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    complianceStatementConnector.deleteComplianceStatementApplication().map{
      _ => Redirect(routes.ApplicationHubController.show())
    }
  }

}
