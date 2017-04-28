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
import models.throttling.TokenModel
import play.api.libs.json.{JsObject, JsString}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object TokenConnector extends TokenConnector with ServicesConfig {
  val serviceUrl = FrontendAppConfig.submissionUrl
  override lazy val http = WSHttp
}

trait TokenConnector {
  val serviceUrl: String
  val http: HttpGet with HttpPost with HttpPut with HttpDelete

  def generateTemporaryToken(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    /*Todo Uncomment
    http.POSTEmpty[HttpResponse](s"$serviceUrl/investment-tax-relief-submission/generate-temporary-token")*/
    Future{
      HttpResponse(200,Some(JsObject(Seq("token" -> JsString("TOK123456789")))))
    }
}

  def validateTemporaryToken(token: Option[TokenModel])(implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    /*Todo Uncomment
    token match {
      case Some(t) => http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief-submission/validate-temporary-token/$t.token")
      case None => Future.successful(Some(false))
    }*/

    Future {
      Some(true)
    }
  }
}
