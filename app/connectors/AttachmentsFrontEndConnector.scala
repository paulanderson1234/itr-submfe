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

package connectors

import auth.TAVCUser
import config.{FrontendAppConfig, WSHttp}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpPost, HttpReads, HttpResponse}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

object AttachmentsFrontEndConnector extends AttachmentsFrontEndConnector with ServicesConfig {
  val internalAttachmentsUrl = FrontendAppConfig.internalAttachmentsUrl
  val http = WSHttp
}

trait AttachmentsFrontEndConnector {

  val internalAttachmentsUrl: String
  val http: HttpGet with HttpPost

  def closeEnvelope(tavcRef: String, envelopeId: String)(implicit hc: HeaderCarrier, user: TAVCUser, ec: ExecutionContext): Future[HttpResponse] = {
    val headerCarrier = hc.copy(extraHeaders = hc.extraHeaders ++ Seq("CSRF-Token" -> "nocheck"))
    http.POSTEmpty[HttpResponse](s"$internalAttachmentsUrl/internal/$tavcRef/$envelopeId/${user.internalId}/close-envelope")(
      HttpReads.readRaw,headerCarrier, ec)
  }
}
