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

import config.{FrontendAppConfig, WSHttp}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.Future

object AttachmentsConnector extends AttachmentsConnector with ServicesConfig {
  val serviceUrl = FrontendAppConfig.attachmentsServiceUrl
  override lazy val http = WSHttp
}

trait AttachmentsConnector {
  val serviceUrl: String
  val http: HttpGet with HttpPost with HttpPut with HttpDelete

  def getEnvelopeStatus(envelopeId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.GET[HttpResponse](s"$serviceUrl/investment-tax-relief-attachments/file-upload/envelope/$envelopeId/get-envelope-status")
  }

  def getFileData(envelopeId: String, fileId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.GET[HttpResponse](s"$serviceUrl/investment-tax-relief-attachments/file-upload/envelope/$envelopeId/file/$fileId/get-file-data")
  }
}
