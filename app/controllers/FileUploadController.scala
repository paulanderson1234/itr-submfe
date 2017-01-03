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

import utils.MultipartFormDataParser._
import config.FrontendGlobal.internalServerErrorTemplate
import auth.AuthorisedAndEnrolledForTAVC
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.EnrolmentConnector
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.mvc.{Action, MultipartFormData}
import play.api.mvc.BodyParsers.parse._
import services.FileUploadService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.Transformers
import views.html.fileUpload.FileUpload

import scala.concurrent.Future

object FileUploadController extends FileUploadController {
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val fileUploadService = FileUploadService
}

trait FileUploadController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val fileUploadService: FileUploadService

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    for {
      envelopeID <- fileUploadService.getEnvelopeID()
      files <- fileUploadService.getEnvelopeFiles(envelopeID)
    } yield (envelopeID, files) match {
      case (_, _) if envelopeID.nonEmpty => Ok(FileUpload(files, envelopeID))
      case (_, _) => InternalServerError(internalServerErrorTemplate)
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Redirect(routes.CheckAnswersController.show()))
  }

  def upload: Action[MultipartFormData[Array[Byte]]] = Action.async(multipartFormData(multipartFormDataParser)) {
    implicit request =>
      val envelopeID = request.body.dataParts("envelope-id").head
      fileUploadService.belowFileNumberLimit(envelopeID).flatMap {
        case true =>
          if (request.body.file("supporting-docs").isDefined) {
            val file = request.body.file("supporting-docs").get
            fileUploadService.validateFile(envelopeID, file.filename, file.ref.length).flatMap {
              case Seq(true, true, true) =>
                fileUploadService.uploadFile(file.ref, file.filename, envelopeID).map {
                  case response if response.status == OK => Redirect(routes.FileUploadController.show())
                  case _ => InternalServerError(internalServerErrorTemplate)
                }
              case errors =>
                fileUploadService.getEnvelopeFiles(envelopeID).map {
                  files => BadRequest(FileUpload(files, envelopeID, generateFormErrors(errors)))
                }
            }
          }
          else Future.successful(Redirect(routes.FileUploadController.show()))
        case false => Future.successful(Redirect(routes.FileUploadController.show()))
      }
  }

  private def generateFormErrors(errors: Seq[Boolean]): Seq[FormError] = {
    val messages = Seq(
      "duplicate-name" -> Messages("page.fileUpload.limit.name"),
      "over-size-limit" -> Messages("page.fileUpload.limit.size"),
      "invalid-format" -> Messages("page.fileUpload.limit.type")
    )
    def createSequence(index: Int = 0, output: Seq[(String, String)] = Seq()): Seq[(String, String)] = {
      if (!errors(index))
        if (index < errors.length - 1) createSequence(index + 1, output :+ messages(index))
        else output :+ messages(index)
      else if (index < errors.length - 1) createSequence(index + 1, output)
      else output
    }
    Transformers.errorBuilder(createSequence())
  }


}
