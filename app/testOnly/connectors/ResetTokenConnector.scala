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

//$COVERAGE-OFF$Disabling scoverage on this test only connector as it is only required by our acceptance test

package testOnly.connectors

import config.{FrontendAppConfig, WSHttp}
import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object ResetTokenConnector extends ResetTokenConnector with ServicesConfig {
  val serviceUrl = FrontendAppConfig.submissionUrl
  val http = WSHttp
}

trait ResetTokenConnector {
  val serviceUrl: String
  val http: HttpGet

  def resetTokens()(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    lazy val tokenResetUrl = s"$serviceUrl/investment-tax-relief/test-only/reset-tokens"

    http.GET[HttpResponse](tokenResetUrl).map {
      response =>
        response.status match {
          case OK => // don't log if OK
          case otherResponse =>
            Logger.warn(s"ResetTokenConnector.resetTokens: url=$tokenResetUrl")
            Logger.warn(s"ResetTokenConnector.resetTokens: unexpected status=$otherResponse\nbody=${response.body}")
        }
        response
    }
  }
}

// $COVERAGE-ON$
