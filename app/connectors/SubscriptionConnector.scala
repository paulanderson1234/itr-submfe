/*
 * Copyright 2016 HM Revenue & Customs
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
import models.{AnnualTurnoverCostsModel, ProposedInvestmentModel, SubscriptionDetailsModel}
import models.submission.{DesSubmitAdvancedAssuranceModel, Submission}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object SubscriptionConnector extends SubscriptionConnector with ServicesConfig {
  val serviceUrl = baseUrl("investment-tax-relief-subscription")
  val http = WSHttp
}

trait SubscriptionConnector {
  val serviceUrl: String
  val http: HttpGet with HttpPost with HttpPut

  def getSubscriptionDetails(tavcReferenceNumber: String)(implicit hc: HeaderCarrier): Future[Option[HttpResponse]] =
    Try (http.GET[Option[HttpResponse]](s"$serviceUrl/investment-tax-relief-subscription/$tavcReferenceNumber/subscription")) match {
      case Success(response) => response
      case Failure(exception) =>
        Logger.error(s"[SubscriptionConnector][getSubscriptionDetails] - Unexpected HTTP GET error. Message=${exception.getMessage}")
        Future.successful(None)
    }
}
