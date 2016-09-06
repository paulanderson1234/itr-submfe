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

import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
import models._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object LifetimeLimitHelper extends LifetimeLimitHelper {
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait LifetimeLimitHelper {

  //implicit val hc = HeaderCarrier()

  val keyStoreConnector: KeystoreConnector

  //Future of all previous schemes as vector
  def previousSchemesFut(implicit headerCarrier: HeaderCarrier): Future[Vector[PreviousSchemeModel]] = {
    PreviousSchemesHelper.getAllInvestmentFromKeystore(keyStoreConnector)
  }

  def previousSchemesAmount()(implicit headerCarrier: HeaderCarrier): Future[Int] = {
    previousSchemesFut.map(previousSchemes => previousSchemes.foldLeft(0)(_ + _.investmentAmount))
  }

  def exceedsLifetimeLogic(isKi: Boolean)(implicit headerCarrier: HeaderCarrier): Future[Boolean] = {
    // Future of proposed investment,
    val proposedInvestmentAmountFut = keyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel](KeystoreKeys.proposedInvestment).map {
      case Some(proposedInvestment) => proposedInvestment.investmentAmount
      case None => 0
    }

    // Addition of two future Int values, returns Future of combined value
    val combinedAmount = for {
      proposedInvestment <- proposedInvestmentAmountFut.map(proposedInvestmentAmountFut => proposedInvestmentAmountFut)
      previousSchemesAmount <- previousSchemesAmount().map(previousSchemesAmountFut => previousSchemesAmountFut)
    } yield proposedInvestment + previousSchemesAmount

    //match statement returns an integer value (12 or 20 million) depending on the KI flag which the map uses to check if combined value is less than
    combinedAmount.map(combinedAmount => combinedAmount <= (isKi match {
      case true => Constants.lifetimeLogicLimitKi
      case false => Constants.lifetimeLogicLimitNotKi
    }))
  }

}