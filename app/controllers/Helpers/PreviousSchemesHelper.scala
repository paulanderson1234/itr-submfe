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
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import utils.Validation
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object PreviousSchemesHelper extends PreviousSchemesHelper {

}

trait PreviousSchemesHelper {

  def getExistingInvestmentFromKeystore(s4lConnector: connectors.S4LConnector,
                                        modelProcessingIdToRetrieve: Int)
                                       (implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[PreviousSchemeModel]] = {

    val idNotFound: Int = -1

    require(modelProcessingIdToRetrieve > 0, "The item to retrieve processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => {
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

  def getAllInvestmentFromKeystore(s4lConnector: connectors.S4LConnector)
                                  (implicit hc: HeaderCarrier, user: TAVCUser): Future[Vector[PreviousSchemeModel]] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => data
      case None =>  Vector[PreviousSchemeModel]()
    }.recover { case _ =>  Vector[PreviousSchemeModel]() }

    result
  }

  def getPreviousInvestmentTotalFromKeystore(s4lConnector: connectors.S4LConnector)
                                            (implicit hc: HeaderCarrier, user: TAVCUser): Future[Int] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => data.foldLeft(0)(_ + _.investmentAmount)
      case None =>  0
    }.recover { case _ =>  0 }

    result
  }

  def getPreviousInvestmentsFromStartDateTotal(s4lConnector: connectors.S4LConnector)
                                              (implicit hc: HeaderCarrier, user: TAVCUser): Future[Int] = {

    def calculateAmount(dateTradeStarted: Option[DateOfIncorporationModel],
                        previousInvestments : Option[Vector[PreviousSchemeModel]]):Int = {

      if (dateTradeStarted.isDefined) 0
      else {

        val startDate = dateTradeStarted.get

        previousInvestments match {
          case Some(data) => data.filter(investment => Validation.dateSinceOtherDate(investment.day.get,
            investment.month.get, investment.year.get, Validation.constructDate(startDate.day.get, startDate.month.get,
              startDate.year.get))).foldLeft(0)(_ + _.investmentAmount)
          case _ => 0
        }
      }
    }

    for{
      dateTradeStarted <- s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
      previousInvestments <- s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes)

      amount = calculateAmount(dateTradeStarted, previousInvestments)
    } yield amount

  }

//  def getPreviousInvestmentsFromStartDateTotal(s4lConnector: connectors.S4LConnector)
//                                            (implicit hc: HeaderCarrier, user: TAVCUser): Future[Int] = {
//
//
//    def calculateAmount(dateTradeStarted: Option[DateOfIncorporationModel],
//                        previousInvestments : Option[Vector[PreviousSchemeModel]]) {
//
//      if (dateTradeStarted.isDefined) 0
//      else {
//
//       val startDate = dateTradeStarted.get
//
//        previousInvestments match {
//
//         // case Some(data) => data.filter(x =>  isBeforeDate(x.year.getOrElse(0))).foldLeft(0)(_ + _.investmentAmount)
//          case Some(data) => data.filter(x =>  Validation.dateSinceOtherDate(x.day.get, x.month.get, x.year.get,
//            Validation.constructDate(startDate.day.get, startDate.month.get, startDate.year.get))).foldLeft(0)(_ + _.investmentAmount)
//          case _ => 0
//        }
//      }
//
//
//    }
//
//    for{
//       dateTradeStarted <- s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
//       previousInvestments <- s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).recover {
//         case _ => None
//       }
//       amount <- calculateAmount(dateTradeStarted, previousInvestments)
//    } yield amount
//
//
////    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
////      case Some(data) => {
////
////        //data.filter(_.year.getOrElse(0) != 2).foldLeft(0)(_ + _.investmentAmount)
////        data.filter(_.year.getOrElse(0) != 2).foldLeft(0)(_ + _.investmentAmount)
////        data.filter(x => isBeforeDate(x.year.getOrElse(0))).foldLeft(0)(_ + _.investmentAmount)
////        //data.foldLeft(0)(_ + _.investmentAmount)
////      }
////      case None =>  0
////    }.recover { case _ =>  0 }
//
//    //result
//  }


  def addPreviousInvestmentToKeystore(s4lConnector: connectors.S4LConnector,
                                      previousSchemeModelToAdd: PreviousSchemeModel)
                                     (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {
    val defaultId: Int = 1

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => {
        val newId = data.last.processingId.get + 1
        data :+ previousSchemeModelToAdd.copy(processingId = Some(newId))
      }
      case None => Vector.empty :+ previousSchemeModelToAdd.copy(processingId = Some(defaultId))
    }.recover { case _ => Vector.empty :+ previousSchemeModelToAdd.copy(processingId = Some(defaultId)) }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.previousSchemes, newVectorList))
  }

  def updateKeystorePreviousInvestment(s4lConnector: connectors.S4LConnector,
                                       previousSchemeModelToUpdate: PreviousSchemeModel)
                                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {
    val idNotFound: Int = -1

    require(previousSchemeModelToUpdate.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => {
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          previousSchemeModelToUpdate.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          data.updated(itemToUpdateIndex, previousSchemeModelToUpdate)
        }
        else data
      }
      case None => Vector[PreviousSchemeModel]()
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.previousSchemes, updatedVectorList))
  }

  def removeKeystorePreviousInvestment(s4lConnector: connectors.S4LConnector, modelProcessingIdToremove: Int)
                                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {

    require(modelProcessingIdToremove > 0, "The modelProcessingIdToremove must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => data.filter(_.processingId.getOrElse(0) != modelProcessingIdToremove)
      case None => Vector[PreviousSchemeModel]()
    }.recover { case _ => Vector[PreviousSchemeModel]() }
    result.flatMap(deletedVectorList => s4lConnector.saveFormData(KeystoreKeys.previousSchemes, deletedVectorList))
  }

  def clearPreviousInvestments(s4lConnector: connectors.S4LConnector)
                                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {
    s4lConnector.saveFormData(KeystoreKeys.previousSchemes, Vector[PreviousSchemeModel]())
  }

}
