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
import uk.gov.hmrc.play.config.ServicesConfig
import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpGet, HttpPost, HttpPut }
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

object ThrottleConnector extends ThrottleConnector with ServicesConfig {
  val serviceUrl = FrontendAppConfig.submissionUrl
  val http = WSHttp
}

trait ThrottleConnector {
  val serviceUrl: String
  val http: HttpGet

  def checkUserAccess()(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/throttle/check-user-access")
}
