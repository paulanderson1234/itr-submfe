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

package controllers.Helpers

import auth.TAVCUser
import common.KeystoreKeys
import models._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object EisSeisHelper extends EisSeisHelper {

}

trait EisSeisHelper {

  def setStartDateCondition(s4lConnector: connectors.S4LConnector, tradeStartConditionIneligible: Boolean)
                           (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {

    // update model (or create first) and set condition
    val result = s4lConnector.fetchAndGetFormData[EisSeisProcessingModel](KeystoreKeys.eisSeisProcessingModel).map {
      case Some(data) => {

        data.copy(ineligibleTradeStartCondition = Some(tradeStartConditionIneligible))
      }
      case None => EisSeisProcessingModel(ineligibleTradeStartCondition = Some(tradeStartConditionIneligible))
    }
    result.flatMap(updatedModel => s4lConnector.saveFormData(KeystoreKeys.eisSeisProcessingModel, updatedModel))
  }

  def setIneligiblePreviousSchemeTypeCondition(s4lConnector: connectors.S4LConnector, previousSchemeTypeConditionIneligible: Boolean)
                                              (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {

    // update model (or create first) and set condition
    val result = s4lConnector.fetchAndGetFormData[EisSeisProcessingModel](KeystoreKeys.eisSeisProcessingModel).map {
      case Some(data) => {

        data.copy(ineligiblePreviousSchemeTypeCondition = Some(previousSchemeTypeConditionIneligible))
      }
      case None => EisSeisProcessingModel(ineligiblePreviousSchemeTypeCondition = Some(previousSchemeTypeConditionIneligible))
    }
    result.flatMap(updatedModel => s4lConnector.saveFormData(KeystoreKeys.eisSeisProcessingModel, updatedModel))
  }

  def setIneligiblePreviousSchemeThresholdCondition(s4lConnector: connectors.S4LConnector, previousSchemeThresholdConditionIneligible: Boolean)
                                                   (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {

    // update model (or create first) and set condition
    val result = s4lConnector.fetchAndGetFormData[EisSeisProcessingModel](KeystoreKeys.eisSeisProcessingModel).map {
      case Some(data) => {

        data.copy(ineligiblePreviousSchemeThresholdCondition = Some(previousSchemeThresholdConditionIneligible))
      }
      case None => EisSeisProcessingModel(ineligiblePreviousSchemeThresholdCondition = Some(previousSchemeThresholdConditionIneligible))
    }
    result.flatMap(updatedModel => s4lConnector.saveFormData(KeystoreKeys.eisSeisProcessingModel, updatedModel))
  }

  def isSeisIneligible(s4lConnector: connectors.S4LConnector)
                                                   (implicit hc: HeaderCarrier, user: TAVCUser): Future[Boolean] = {

    // update model (or create first) and set condition
    val result = s4lConnector.fetchAndGetFormData[EisSeisProcessingModel](KeystoreKeys.eisSeisProcessingModel).map {
      case Some(data) => Future(data.isSeisIneligible)
      case None => Future(false)
    }

    result.flatMap(isEligible => isEligible)

  }

}
