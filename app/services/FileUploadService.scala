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
import connectors.AttachmentsFrontEndConnector
import play.Logger
import play.mvc.Http.Status._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

object FileUploadService extends FileUploadService {
  override val attachmentsFrontEndConnector = AttachmentsFrontEndConnector
  override def getUploadFeatureEnabled: Boolean = FrontendAppConfig.uploadFeatureEnabled
}

trait FileUploadService {

  val attachmentsFrontEndConnector: AttachmentsFrontEndConnector

  def getUploadFeatureEnabled: Boolean

  def closeEnvelope(tavcRef: String, envelopeId: String)(implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[HttpResponse] = {
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
}
