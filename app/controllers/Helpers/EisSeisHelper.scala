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

import akka.pattern.FutureRef
import auth.TAVCUser
import utils.Validation
import common.KeystoreKeys
import models._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object EisSeisHelper extends EisSeisHelper {

}

trait EisSeisHelper {

  /** Helper method to determine if the current state of the application is ineligible for SEIS
    *
    * returns future(True) if already ineligible for SEIS, or Future(false) otherwise.
    */
  def isIneligibleForSeis(s4lConnector: connectors.S4LConnector)
                         (implicit hc: HeaderCarrier, user: TAVCUser): Future[Boolean] = {

    val result = s4lConnector.fetchAndGetFormData[EisSeisProcessingModel](KeystoreKeys.eisSeisProcessingModel).map {
      case Some(data) =>
        Future(data.isSeisIneligible)
      case None => Future(false)
    }

    result.flatMap(isEligible => isEligible)

  }

  /** Helper method to set the SEIS ineligibility flag relating to the trade start condition.
    *
    * @param s4lConnector                  An instance of the Save4Later Connector.
    * @param tradeStartConditionIneligible Boolean indicating how the SEIS trade start ineligibility condition should be set.
    */
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

  /** Helper method to set the SEIS ineligibility flag relating to the previous scheme types condition.
    *
    * @param s4lConnector                          An instance of the Save4Later Connector.
    * @param previousSchemeTypeConditionIneligible Boolean indicating how the SEIS previous scheme type ineligibility condition should be set.
    */
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

  /** Helper method to set the SEIS ineligibility flag relating to the previous scheme threshold exceeded condition.
    *
    * @param s4lConnector                               An instance of the Save4Later Connector.
    * @param previousSchemeThresholdConditionIneligible Boolean indicating how the SEIS previous scheme threshold exceeded ineligibility condition should be set.
    */
  def setIneligiblePreviousSchemeThresholdCondition(s4lConnector: connectors.S4LConnector, previousSchemeThresholdConditionIneligible: Boolean)
                                                   (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {

    // update model (or create first) and set condition
    val result = s4lConnector.fetchAndGetFormData[EisSeisProcessingModel](KeystoreKeys.eisSeisProcessingModel).map {
      case Some(data) =>
        data.copy(ineligiblePreviousSchemeThresholdCondition = Some(previousSchemeThresholdConditionIneligible))
      case None =>
        EisSeisProcessingModel(ineligiblePreviousSchemeThresholdCondition = Some(previousSchemeThresholdConditionIneligible))
    }
    result.flatMap(updatedModel => s4lConnector.saveFormData(KeystoreKeys.eisSeisProcessingModel, updatedModel))
  }

  /** Helper method to set the SEIS ineligibility flag relating to the previous scheme threshold exceeded condition.
    *
    * @param s4lConnector        An instance of the Save4Later Connector.
    * @param tradeStartDateModel The current valid trade start date model entered.
    */
  def shouldDisplayTradeStartDateError(s4lConnector: connectors.S4LConnector, tradeStartDateModel: TradeStartDateModel)
                                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[Boolean] = {

    def shouldDisplay(existingStartDate: Option[TradeStartDateModel], eisSeisProcessingModel: Option[EisSeisProcessingModel]): Future[Boolean] = {
      if (existingStartDate.nonEmpty && dateChangedCheck(existingStartDate.get, tradeStartDateModel)) Future(true)
      else {
        eisSeisProcessingModel match {
          case Some(data) => Future(!data.isSeisIneligible)
          case _ => Future(true)
        }
      }
    }

    (for {
      existingStartDate: Option[TradeStartDateModel] <- s4lConnector.fetchAndGetFormData[TradeStartDateModel](KeystoreKeys.tradeStartDate)
      processingModel <- s4lConnector.fetchAndGetFormData[EisSeisProcessingModel](KeystoreKeys.eisSeisProcessingModel)

    } yield shouldDisplay(existingStartDate, processingModel)).flatMap(result => result)

  }

  private def dateChangedCheck(tradeStartDateModel: TradeStartDateModel, existingStartDateModel: TradeStartDateModel): Boolean = {
    Validation.isNotSameDate(
      Validation.constructDate(
        tradeStartDateModel.tradeStartDay.get, tradeStartDateModel.tradeStartMonth.get, tradeStartDateModel.tradeStartYear.get),
      Validation.constructDate(
        existingStartDateModel.tradeStartDay.get, existingStartDateModel.tradeStartMonth.get, existingStartDateModel.tradeStartYear.get))
  }

}