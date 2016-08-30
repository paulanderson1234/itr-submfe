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

package controllers

import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
import controllers.predicates.ValidActiveSession
import forms.UsedInvestmentReasonBeforeForm._
import models.UsedInvestmentReasonBeforeModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._

import scala.concurrent.Future
import views.html.investment.UsedInvestmentReasonBefore

object UsedInvestmentReasonBeforeController extends UsedInvestmentReasonBeforeController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait UsedInvestmentReasonBeforeController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](KeystoreKeys.usedInvestmentReasonBefore).map {
      case Some(data) => Ok(UsedInvestmentReasonBefore(usedInvestmentReasonBeforeForm.fill(data)))
      case None => Ok(UsedInvestmentReasonBefore(usedInvestmentReasonBeforeForm))
    }
  }

  val submit = Action.async { implicit request =>
    usedInvestmentReasonBeforeForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(UsedInvestmentReasonBefore(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.usedInvestmentReasonBefore, validFormData)
        validFormData.usedInvestmentReasonBefore match {
          case Constants.StandardRadioButtonYesValue => {
            Future.successful(Redirect(routes.PreviousBeforeDOFCSController.show()))
          }
          case Constants.StandardRadioButtonNoValue => {
            keyStoreConnector.saveFormData(KeystoreKeys.backLinkNewGeoMarket, routes.UsedInvestmentReasonBeforeController.show().toString())
            Future.successful(Redirect(routes.NewGeographicalMarketController.show()))
          }
        }
      }
    )
  }
}
