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

package services

import auth.TAVCUser
import common.KeystoreKeys
import connectors.{S4LConnector, SubmissionConnector}
import models.registration.{ETMPRegistrationDetailsModel, RegistrationDetailsModel}
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

object RegistrationDetailsService extends RegistrationDetailsService {
  override lazy val submissionConnector = SubmissionConnector
  override lazy val s4lConnector = S4LConnector
  override lazy val subscriptionService = SubscriptionService
}

trait RegistrationDetailsService {

  val submissionConnector: SubmissionConnector
  val s4lConnector: S4LConnector
  val subscriptionService: SubscriptionService

  def getRegistrationDetails(tavcRef: String)(implicit hc: HeaderCarrier, user: TAVCUser, ec: ExecutionContext): Future[Option[RegistrationDetailsModel]] = {
    s4lConnector.fetchAndGetFormData[RegistrationDetailsModel](KeystoreKeys.registrationDetails).flatMap[Option[RegistrationDetailsModel]] {
      case Some(registrationDetailsModel) => Future.successful(Some(registrationDetailsModel))
      case None => subscriptionService.getEtmpSubscriptionDetails(tavcRef).flatMap[Option[RegistrationDetailsModel]] {
        case Some(subscriptionTypeModel) => submissionConnector.getRegistrationDetails(subscriptionTypeModel.safeId).map {
          registrationDetailsModel =>
            registrationDetailsModel.json.validate[RegistrationDetailsModel](ETMPRegistrationDetailsModel.readsRDM) match {
              case data: JsSuccess[RegistrationDetailsModel] => {
                s4lConnector.saveFormData(KeystoreKeys.registrationDetails, data.value)
                Some(data.value)
              }
              case e: JsError => {
                Logger.warn(s"[RegistrationDetailsServce][getRegistrationDetails] - Failed to parse JSON response. Errors=${e.errors}")
                None
              }
            }
        }.recover {
          case _ => {
            Logger.warn(s"[RegistrationDetailsServce][getRegistrationDetails] - No Registration Details Retrieved")
            None
          }
        }
        case None => Future.successful(None)
      }
    }
  }

}
