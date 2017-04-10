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

package controllers.eis

import auth.{AuthorisedAndEnrolledForTAVC, EIS, VCT}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import services.FileUploadService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.supportingDocuments.SupportingDocuments
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object SupportingDocumentsController extends SupportingDocumentsController
{
  override lazy val s4lConnector: S4LConnector = S4LConnector
  val attachmentsFrontEndUrl = applicationConfig.attachmentFileUploadUrl("eis")
  val fileUploadService: FileUploadService = FileUploadService
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait SupportingDocumentsController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS), Seq(VCT), Seq(EIS, VCT))

  val attachmentsFrontEndUrl: String
  val fileUploadService: FileUploadService

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    if (fileUploadService.getUploadFeatureEnabled) {
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
