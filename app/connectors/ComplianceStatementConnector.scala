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
import models.internal.CSApplicationModel
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSDelete

import scala.concurrent.Future

object ComplianceStatementConnector extends ComplianceStatementConnector with ServicesConfig {
  val serviceUrl = FrontendAppConfig.internalCSSubmissionUrl
  val http = WSHttp
}

trait ComplianceStatementConnector {

  val serviceUrl: String
  val http: HttpGet with HttpPost

  def getComplianceStatementApplication()(implicit hc: HeaderCarrier): Future[CSApplicationModel] = {
    val headerCarrier = hc.copy(extraHeaders = hc.extraHeaders ++ Seq("CSRF-Token" -> "nocheck"))
    http.GET[CSApplicationModel](s"$serviceUrl/internal/cs-application-in-progress")(implicitly[HttpReads[CSApplicationModel]], headerCarrier)
  }

  def deleteComplianceStatementApplication()(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val headerCarrier = hc.copy(extraHeaders = hc.extraHeaders ++ Seq("Csrf-Token" -> "nocheck"))
    http.POSTEmpty[HttpResponse](s"$serviceUrl/internal/delete-cs-application")(HttpReads.readRaw, headerCarrier)
  }
}
