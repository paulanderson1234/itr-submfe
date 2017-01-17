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
import play.api.Logger
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object AttachmentsFrontEndConnector extends AttachmentsFrontEndConnector with ServicesConfig {
  val attachmentsFrontEndUrl = FrontendAppConfig.attachmentsFrontEndServiceBaseUrl
  val http = WSHttp
}

trait AttachmentsFrontEndConnector {

  val attachmentsFrontEndUrl: String
  val http: HttpGet with HttpPost

  // Note: Unlike keystore, S4L is encrypted with a service specific encryption key so we can't interrogate another service s4l
  // directly like  we can with Keystore. We will therefore need to call the service (front-end) that encrypted it to send it back.
  def getEnvelopeId(implicit hc: HeaderCarrier): Future[Option[String]] = {
    http.GET[Option[String]](s"$attachmentsFrontEndUrl/envelopeId")
  }

  def closeEnvelope(tavcRef: String)(implicit hc: HeaderCarrier, user: TAVCUser): Future[HttpResponse] = {
    http.POSTEmpty[HttpResponse](s"${attachmentsFrontEndUrl}/$tavcRef/close-envelope")
  }
}
