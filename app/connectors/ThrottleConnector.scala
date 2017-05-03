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

import config.WSHttp
import play.api.Logger
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ThrottleConnector extends ThrottleConnector with ServicesConfig {
  val serviceUrl = baseUrl("investment-tax-relief-submission")
  val http = WSHttp
}

trait ThrottleConnector {
  val serviceUrl: String
  val http: HttpGet with HttpPost with HttpPut

  def checkUserAccess()(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/throttle/check-user-access")
}
