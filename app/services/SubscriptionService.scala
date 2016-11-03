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

package services

import auth.TAVCUser
import common.KeystoreKeys
import connectors.{S4LConnector, SubscriptionConnector}
import models._
import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

trait SubscriptionService {

  val subscriptionConnector: SubscriptionConnector
  val s4lConnector: S4LConnector

  def getEtmpSubscriptionDetails(tavcRef: String)(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[SubscriptionDetailsModel]] = {
    s4lConnector.fetchAndGetFormData[SubscriptionDetailsModel](KeystoreKeys.subscriptionDetails).flatMap[Option[SubscriptionDetailsModel]] {
      case Some(subscriptionData) => Future.successful(Some(subscriptionData))
      case _ => subscriptionConnector.getSubscriptionDetails(tavcRef) map {
        case Some(subscriptionDetails) =>
          Try(subscriptionDetails.json.as[SubscriptionDetailsModel](EtmpSubscriptionDetailsModel.streads)) match {
            case Success(subscriptionData) =>
              s4lConnector.saveFormData[SubscriptionDetailsModel](KeystoreKeys.subscriptionDetails, subscriptionData)
              Some(subscriptionData)
            case Failure(ex) =>
              Logger.warn(s"[SubscriptionService][getEtmpSubscriptionDetails] - Failed to parse JSON response. Message=${ex.getMessage}")
              None
          }
        case _ =>
          Logger.warn(s"[SubscriptionService][getEtmpSubscriptionDetails] - No Subscription Details Retrieved")
          None
      }
    }
  }

  def getSubscriptionContactDetails(tavcRef: String)(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[ContactDetailsModel]] =
    getEtmpSubscriptionDetails(tavcRef).map(_.fold[Option[ContactDetailsModel]](None)(data => Some(data.contactDetails)))

  def getSubscriptionContactAddress(tavcRef: String)(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[AddressModel]] =
    getEtmpSubscriptionDetails(tavcRef).map(_.fold[Option[AddressModel]](None)(data => Some(data.contactAddress)))
}

object SubscriptionService extends SubscriptionService {
  val subscriptionConnector = SubscriptionConnector
  val s4lConnector = S4LConnector
}
