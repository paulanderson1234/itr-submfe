/*
 * Copyright 2016 HM Revenue & Customs
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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.EnrolmentConnector
import play.api.mvc.{Action, AnyContent}
import services.FileUploadService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import forms.FileDeleteForm._
import views.html.fileUpload.FileDelete

import scala.concurrent.Future

object FileDeleteController extends FileDeleteController {
  override lazy val fileUploadService = FileUploadService
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
}

trait FileDeleteController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val fileUploadService: FileUploadService

  def show (fileID: String): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    fileUploadService.getEnvelopeID(false).flatMap {
      envelopeID => fileUploadService.getEnvelopeFiles(envelopeID).map {
        files => val file = files.filter(f => f.id.equals(fileID)).head
          Ok(FileDelete(fileID, file.name))
      }
    }
  }

  def submit(): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    fileDeleteForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(InternalServerError(internalServerErrorTemplate))
      },
      validFormData => {
        fileUploadService.deleteFile(validFormData.fileID).map {
          result => result.status match {
            case OK => Redirect(routes.FileUploadController.show())
            case _ => InternalServerError(internalServerErrorTemplate)
          }
        }
      }
    )
  }

}
