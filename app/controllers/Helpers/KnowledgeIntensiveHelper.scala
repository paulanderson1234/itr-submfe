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
import utils.Validation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object KnowledgeIntensiveHelper extends KnowledgeIntensiveHelper {

}

trait KnowledgeIntensiveHelper {

  def setKiDateCondition(s4lConnector: connectors.S4LConnector, dateDay:Int, dateMonth:Int, dateYear:Int)
                        (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {

    // check params
    require(dateDay > 0 && dateDay < 32, "The item to update processingId must be an integer > 0")
    require(dateMonth > 0 && dateMonth < 13, "The item to update processingId must be an integer > 0")
    require(dateYear >= 1000, "The item to update processingId must be an integer > 0")

    // update kimodel (or create first) and  dateConditionMet to
    val result = s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel).map {
      case Some(data) => {
        data.copy(dateConditionMet = Some(!Validation.dateAfterIncorporationRule(dateDay, dateMonth, dateYear)))
      }
      case None => KiProcessingModel(dateConditionMet = Some(!Validation.dateAfterIncorporationRule(dateDay, dateMonth, dateYear)))
    }
    result.flatMap(updatedKiModel => s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, updatedKiModel))
  }

  def setCompanyAssertsKi(s4lConnector: connectors.S4LConnector, companyAssertsIsKi: Boolean)
                         (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {

    // update kimodel (or create first) and  dateConditionMet to
    val result = s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel).map {
      case Some(data) => {

        data.copy(companyAssertsIsKi = Some(companyAssertsIsKi))
      }
      case None => KiProcessingModel(companyAssertsIsKi = Some(companyAssertsIsKi))
    }
    result.flatMap(updatedKiModel => s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, updatedKiModel))
  }
}
