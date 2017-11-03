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

package controllers.schemeSelection

import auth.AuthorisedAndEnrolledForTAVC
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{ComplianceStatementConnector, EnrolmentConnector, S4LConnector}
import play.api.mvc.{Action, AnyContent, Request, Result}
import forms.schemeSelection.SchemeSelectionForm._
import models.submission.SchemeTypesModel
import play.api.Logger
import views.html.schemeSelection.SchemeSelection
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object SchemeSelectionController extends SchemeSelectionController {
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val s4lConnector = S4LConnector
  val complianceStatementConnector: ComplianceStatementConnector = ComplianceStatementConnector
}

trait SchemeSelectionController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq()
  val complianceStatementConnector: ComplianceStatementConnector

  def show(): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    val fteStatus = s4lConnector.fetchAndGetFormData[SchemeTypesModel](KeystoreKeys.selectedSchemes).flatMap (selectedSchemes => {
      selectedSchemes match {
        case Some(scheme) => Future.successful(Some(scheme), false)
        case _ => complianceStatementConnector.getComplianceStatementApplication().map{
        csAppStatus => (None, csAppStatus.inProgress)
      }}
    })

    fteStatus.map{
      case (Some(scheme), _) => Ok(SchemeSelection(schemeSelectionForm.fill(scheme)))
      case (None, true) => Redirect(controllers.routes.ApplicationHubController.show())
      case (None, false) => Ok(SchemeSelection(schemeSelectionForm))
    }
  }

  def submit(): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    schemeSelectionForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(SchemeSelection(formWithErrors)))
      },
      validFormData => {
        (for {
          saveSchemes <- s4lConnector.saveFormData(KeystoreKeys.selectedSchemes, validFormData)
          saveApplication <- s4lConnector.saveFormData(KeystoreKeys.applicationInProgress, true)
        } yield (saveSchemes, saveApplication)).map {
          result => routeToScheme(validFormData)
        }.recover {
          case e: Exception => Logger.warn(s"[SchemeSelectionController][submit] Error when calling saveFormData: ${e.getMessage}")
            routeToScheme(validFormData)
        }
      }
    )
  }

  private def routeToScheme(schemeTypesModel: SchemeTypesModel)(implicit request: Request[AnyContent]): Result = {
    schemeTypesModel match {
      //EIS Flow
      case SchemeTypesModel(true, false, false, false) => Redirect(controllers.eis.routes.NatureOfBusinessController.show().url)
      //SEIS Flow
      case SchemeTypesModel(false, true, false, false) => Redirect(controllers.seis.routes.NatureOfBusinessController.show().url)
      //VCT Flow
      case SchemeTypesModel(false, false, false, true) => Redirect(controllers.eis.routes.NatureOfBusinessController.show().url)
      //EIS SEIS Flow
      case SchemeTypesModel(true, true, false, false) => Redirect(controllers.eisseis.routes.NatureOfBusinessController.show().url)
      //EIS VCT Flow
      case SchemeTypesModel(true, false, false, true) => Redirect(controllers.eis.routes.NatureOfBusinessController.show().url)
      //SEIS VCT Flow
      case SchemeTypesModel(false, true, false, true) => Redirect(controllers.eisseis.routes.NatureOfBusinessController.show().url)
      //EIS SEIS VCT Flow
      case SchemeTypesModel(true, true, false, true) => Redirect(controllers.eisseis.routes.NatureOfBusinessController.show().url)
      //Invalid Flow
      case _ => BadRequest(SchemeSelection(schemeSelectionForm.fill(schemeTypesModel)))
    }
  }

}
