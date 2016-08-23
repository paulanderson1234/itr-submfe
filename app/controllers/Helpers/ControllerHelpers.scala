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

package controllers.Helpers

import common.KeystoreKeys
import models.PreviousSchemeModel
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ControllerHelpers extends ControllerHelpers {

}

trait ControllerHelpers {

  def getSavedBackLink(keystoreKey: String, keystoreConnector: connectors.KeystoreConnector)
                      (implicit hc: HeaderCarrier): Future[Option[String]] = {
    keystoreConnector.fetchAndGetFormData[String](keystoreKey).flatMap {
      case Some(data) => Future.successful(Some(data))
      case None => Future.successful(None)
    }
  }

  def getExistingInvestmentFromKeystore(keyStoreConnector: connectors.KeystoreConnector,
                                        modelProcessingIdToRetrieve: Int)
                                       (implicit hc: HeaderCarrier): Future[Option[PreviousSchemeModel]] = {

    val idNotFound: Int = -1

    require(modelProcessingIdToRetrieve > 0, "The item to retrieve processingId must be an integer > 0")

    val result = keyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => {
        println(data)
        val itemToRetrieveIndex = data.indexWhere(_.processingId.getOrElse(0) == modelProcessingIdToRetrieve)
        if (itemToRetrieveIndex != idNotFound) {
          Some(data(itemToRetrieveIndex))
        }
        else None
      }
      case None => None
    }.recover { case _ => None }

    result
  }

  def addPreviousInvestmentToKeystore(keyStoreConnector: connectors.KeystoreConnector,
                                      previousSchemeModelToAdd: PreviousSchemeModel)
                                     (implicit hc: HeaderCarrier): Future[CacheMap] = {

    val defaultId: Int = 1
    
    val result = keyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => {
        val newId = data.last.processingId.get + 1
        val newModelWithId = previousSchemeModelToAdd.copy(processingId = Some(newId))
        data :+ newModelWithId
      }
      case None => Vector.empty :+ previousSchemeModelToAdd.copy(processingId = Some(defaultId))
    }.recover { case _ => Vector.empty :+ previousSchemeModelToAdd.copy(processingId = Some(defaultId)) }

    result.flatMap(newVectorList => keyStoreConnector.saveFormData(KeystoreKeys.previousSchemes, newVectorList))
  }

  def updateKeystorePreviousInvestment(keyStoreConnector: connectors.KeystoreConnector,
                                       previousSchemeModelToUpdate: PreviousSchemeModel)
                                      (implicit hc: HeaderCarrier): Future[CacheMap] = {
    val idNotFound: Int = -1

    require(previousSchemeModelToUpdate.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = keyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => {
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          previousSchemeModelToUpdate.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          data.updated(itemToUpdateIndex, previousSchemeModelToUpdate)
        }
        else data
      }
      case None => Vector()
    }.recover { case _ => Vector() }
    result.flatMap(updatedVectorList => keyStoreConnector.saveFormData(KeystoreKeys.previousSchemes, updatedVectorList))
  }

  def removeKeystorePreviousInvestment(keyStoreConnector: connectors.KeystoreConnector, modelProcessingIdToremove: Int)
                                      (implicit hc: HeaderCarrier): Future[CacheMap] = {

    require(modelProcessingIdToremove > 0, "The modelProcessingIdToremove must be an integer > 0")

    val result = keyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => data.filter(_.processingId.getOrElse(0) != modelProcessingIdToremove)
      case None => Vector()
    }.recover { case _ => Vector() }
    result.flatMap(deletedVectorList => keyStoreConnector.saveFormData(KeystoreKeys.previousSchemes, deletedVectorList))
  }

}
