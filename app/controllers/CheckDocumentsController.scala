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
import play.api.mvc.{Action, AnyContent}
import services.FileUploadService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.checkAndSubmit.CheckDocuments
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future

object CheckDocumentsController extends CheckDocumentsController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val fileUploadService = FileUploadService
}

trait CheckDocumentsController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq()

  lazy val fileUploadService = FileUploadService

  def show (envelopeId: Option[String]) : Action[AnyContent] = AuthorisedAndEnrolled.async {
    implicit user => implicit request =>
      if(envelopeId.fold("")(_.toString).length > 0) {
        s4lConnector.saveFormData(KeystoreKeys.envelopeId, envelopeId.getOrElse(""))
      }

      for {
        envelopeId <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.envelopeId)
        files <- fileUploadService.getEnvelopeFiles(envelopeId.get)
      } yield (files, envelopeId) match {
        case (_, _) => Ok(CheckDocuments(files, envelopeId.get))
        case (_, None) => InternalServerError(internalServerErrorTemplate)
      }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Redirect(routes.FileUploadAcknowledgementController.show()))
  }

  val redirectAttachments = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Redirect(applicationConfig.attachmentFileUploadOutsideUrl))
  }
}