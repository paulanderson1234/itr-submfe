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
import models.FileModel
import play.api.mvc.Action
import services.FileUploadService
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future
import views.html.fileUpload.FileUpload



object FileUploadController extends FileUploadController{
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val fileUploadService = FileUploadService
}



trait FileUploadController extends FrontendController with AuthorisedAndEnrolledForTAVC{

  val fileUploadService: FileUploadService

  val lessThanFiveMegabytes: Long => Boolean = bytes => bytes <= Constants.fileSizeLimit

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    fileUploadService.getEnvelopeFiles.map(files => Ok(FileUpload(files)))
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    fileUploadService.closeEnvelope.map {
      result => result.status match {
        case CREATED => Ok("ENVELOPE CLOSED")
        case _ => Ok("UH OH")
      }
    }
  }

  val upload = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    val tempFile = request.body.asMultipartFormData.get.file("supporting-docs").get
    tempFile.ref.moveTo(new File(s"/tmp/${tempFile.filename}"))
    val file = new File(s"/tmp/${tempFile.filename}")
    fileUploadService.uploadFile(file).map {
      response =>
        fileUploadService.checkEnvelopeStatus.map {
          result =>
            file.delete()//bye
            println(result.toString)
        }
        Ok(response.status.toString)
    }
//    request.body.file("supporting-docs").map { document =>
//      if(lessThanFiveMegabytes(document.ref.file.length())) {
//        Ok("File uploaded")
//      } else BadRequest("Too large")
//    }.getOrElse {
//      InternalServerError
//    }
  }

}