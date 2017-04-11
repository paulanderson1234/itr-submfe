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
import common.KeystoreKeys
import config.FrontendGlobal.internalServerErrorTemplate
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import services.FileUploadService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.checkAndSubmit.CheckDocuments

import scala.concurrent.Future

object CheckDocumentsController extends CheckDocumentsController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  val fileUploadService: FileUploadService = FileUploadService
}

trait CheckDocumentsController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq()

  val fileUploadService: FileUploadService

  def show(envelopeId: String): Action[AnyContent] = AuthorisedAndEnrolled.async {
    implicit user => implicit request =>

      s4lConnector.saveFormData(KeystoreKeys.envelopeId, envelopeId)
      for {
        files <- fileUploadService.getEnvelopeFiles(envelopeId)
      } yield (files, envelopeId) match {
        case (_, _) => Ok(CheckDocuments(files, envelopeId))
      }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Redirect(routes.FileUploadAcknowledgementController.show()))
  }

  val redirectAttachments = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Redirect(applicationConfig.attachmentFileUploadOutsideUrl))
  }
}