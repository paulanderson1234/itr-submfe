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

package services

import auth.TAVCUser
import config.FrontendAppConfig
import connectors.{AttachmentsConnector, AttachmentsFrontEndConnector}
import models.fileUpload.{Envelope, EnvelopeFile}
import play.Logger
import play.mvc.Http.Status._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

object FileUploadService extends FileUploadService {
  override val attachmentsFrontEndConnector = AttachmentsFrontEndConnector
  override def getUploadFeatureEnabled: Boolean = FrontendAppConfig.uploadFeatureEnabled
  override lazy val attachmentsConnector = AttachmentsConnector
}

trait FileUploadService {

  val attachmentsFrontEndConnector: AttachmentsFrontEndConnector
  val attachmentsConnector: AttachmentsConnector

  def getUploadFeatureEnabled: Boolean

  def closeEnvelope(tavcRef: String, envelopeId: String)(implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[HttpResponse] = {
    if (envelopeId.nonEmpty) {
      attachmentsFrontEndConnector.closeEnvelope(tavcRef, envelopeId).map {
        result => result.status match {
          case CREATED =>
            result
          case _ => Logger.warn(s"[FileUploadService][closeEnvelope] Error ${result.status} received.")
            result
        }
      }.recover {
        case e: Exception => Logger.warn(s"[FileUploadService][closeEnvelope] Error ${e.getMessage} received.")
          HttpResponse(INTERNAL_SERVER_ERROR)
      }
    }
    else {
      Future(HttpResponse(OK))
    }
  }

  def checkEnvelopeStatus(envelopeID: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Option[Envelope]] = {
    attachmentsConnector.getEnvelopeStatus(envelopeID).map {
      result => result.status match {
        case OK => result.json.asOpt[Envelope]
        case _ =>
          Logger.warn(s"[FileUploadConnector][checkEnvelopeStatus] Error ${result.status} received.")
          None
      }
    }
  }

  def getEnvelopeFiles(envelopeID: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Seq[EnvelopeFile]] = {
    checkEnvelopeStatus(envelopeID).map {
      case Some(envelope) => envelope.files.getOrElse(Seq())
      case _ => Seq()
    }
  }
}