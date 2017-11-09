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
import models.EmailConfirmationModel
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}
import play.mvc.Http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpPost, HttpPut, HttpResponse}

object EmailConfirmationConnector extends EmailConfirmationConnector with ServicesConfig {
  val serviceUrl = FrontendAppConfig.emailUrl
  val domain = FrontendAppConfig.emailDomain
  val http = WSHttp
}

trait EmailConfirmationConnector {
  val serviceUrl: String
  val domain: String
  val http: HttpGet with HttpPost with HttpPut

  def sendEmailConfirmation(emailConfirmationModel: EmailConfirmationModel)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    http.POST[JsValue, HttpResponse](s"$serviceUrl/${domain}/email", Json.toJson(emailConfirmationModel)).recover {
      case _ => Logger.warn(s"[EmailConfirmationConnector][sendEmailConfirmation] - Upstream HTTP Post error")
        HttpResponse(INTERNAL_SERVER_ERROR)
    }
  }
}
