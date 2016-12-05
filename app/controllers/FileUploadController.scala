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

import auth.AuthorisedAndEnrolledForTAVC
import common.Constants
import config.{FrontendAuthConnector, FrontendAppConfig}
import connectors.EnrolmentConnector
import models.FileModel
import play.api.mvc.Action
import uk.gov.hmrc.play.frontend.controller.FrontendController
import scala.concurrent.Future
import views.html.fileUpload.FileUpload



object FileUploadController extends FileUploadController{
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}



trait FileUploadController extends FrontendController with AuthorisedAndEnrolledForTAVC{


  val files = Array(FileModel("test-file-1"), FileModel("test-file-2"))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Ok(FileUpload(files)))
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    /***TODO ***/
    Future.successful(Ok)
  }

  val upload = Action(parse.multipartFormData) { request =>
    request.body.file("supporting-docs").map { document =>
      if(lessThanFiveMegabytes(document.ref.file.length())) Ok("File uploaded") else BadRequest("Too large")
    }.getOrElse {
      InternalServerError
    }
  }

  val lessThanFiveMegabytes: Long => Boolean = bytes => bytes <= Constants.fileSizeLimit

}