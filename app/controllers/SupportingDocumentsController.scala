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

import auth.AuthorisedAndEnrolledForTAVC
import common.{Features, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.supportingDocuments.SupportingDocuments

import scala.concurrent.Future

object SupportingDocumentsController extends SupportingDocumentsController
{
  val s4lConnector: S4LConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val attachmentsFrontEndUrl = applicationConfig.attachmentFileUploadUrl
}

trait SupportingDocumentsController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val s4lConnector: S4LConnector
  val attachmentsFrontEndUrl : String

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    if(Features.UploadCondition) {
      Future.successful(Redirect(routes.SupportingDocumentsUploadController.show()))
    }
    else {
      ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSupportingDocs, s4lConnector).flatMap {
        case Some(backlink) => Future.successful(Ok(SupportingDocuments(backlink)))
        case None => Future.successful(Redirect(routes.ConfirmCorrespondAddressController.show()))
      }
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
      Future.successful(Redirect(routes.CheckAnswersController.show()))
  }

}


