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

import java.io.File

import auth.AuthorisedAndEnrolledForTAVC
import common.Constants
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.EnrolmentConnector
import play.api.data.{Form, FormError}
import services.FileUploadService
import services.FileUploadService._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.Transformers._
import views.html.fileUpload.FileUpload

import scala.concurrent.Future


object FileUploadController extends FileUploadController{
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val fileUploadService = FileUploadService
}



trait FileUploadController extends FrontendController with AuthorisedAndEnrolledForTAVC{

  val fileUploadService: FileUploadService

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    fileUploadService.getEnvelopeFiles.map(files => Ok(FileUpload(files)))
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Redirect(routes.CheckAnswersController.show()))
  }

  val upload = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    val tempFile = request.body.asMultipartFormData.get.file("supporting-docs").get
    val validation = (lessThanFiveMegabytes(tempFile.ref.file), isPDF(tempFile.contentType))
    validation match {
      case (false,_) => Future.successful(Ok)
      case (true, false) => Future.successful(Ok)
    tempFile.ref.moveTo(new File(s"/tmp/${tempFile.filename}"))
    val file = new File(s"/tmp/${tempFile.filename}")
    fileUploadService.uploadFile(file).map {
      response =>
        fileUploadService.checkEnvelopeStatus.map {
          result =>
            file.delete()
        }
        Redirect(routes.FileUploadController.show())
    }
  }
}