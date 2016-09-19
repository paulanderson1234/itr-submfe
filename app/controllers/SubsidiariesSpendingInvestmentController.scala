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

/**
  * Copyright 2016 HM Revenue & Customs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package controllers

import auth.AuthorisedForTAVC
import common.{Constants, KeystoreKeys}
import config.{FrontendAuthConnector, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.Helpers.ControllerHelpers
import forms.SubsidiariesSpendingInvestmentForm._
import models.SubsidiariesSpendingInvestmentModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html._

import scala.concurrent.Future

object SubsidiariesSpendingInvestmentController extends SubsidiariesSpendingInvestmentController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
}

trait SubsidiariesSpendingInvestmentController extends FrontendController with AuthorisedForTAVC{

  val keyStoreConnector: KeystoreConnector

  val show = Authorised.async { implicit user => implicit request =>
    def routeRequest(backUrl: Option[String]) = {
      if(backUrl.isDefined) {
        keyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment).map {
          case Some(data) => Ok(investment.SubsidiariesSpendingInvestment(subsidiariesSpendingInvestmentForm.fill(data), backUrl.get))
          case None => Ok(investment.SubsidiariesSpendingInvestment(subsidiariesSpendingInvestmentForm, backUrl.get))
        }
      }
      else Future.successful(Redirect(routes.WhatWillUseForController.show()))
    }
    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSubSpendingInvestment, keyStoreConnector)(hc)
      route <- routeRequest(link)
    } yield route
  }

  val submit = Authorised.async { implicit user => implicit request =>
    subsidiariesSpendingInvestmentForm.bindFromRequest.fold(
      invalidForm =>
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSubSpendingInvestment, keyStoreConnector)(hc).flatMap {
          case Some(data) => Future.successful(
            BadRequest(views.html.investment.SubsidiariesSpendingInvestment(invalidForm, data)))
          case None => Future.successful(Redirect(routes.WhatWillUseForController.show()))
      },
      validForm => {
        keyStoreConnector.saveFormData(KeystoreKeys.subsidiariesSpendingInvestment, validForm)
        validForm.subSpendingInvestment match {
          case  Constants.StandardRadioButtonYesValue  =>
            Future.successful(Redirect(routes.SubsidiariesNinetyOwnedController.show()))
          case  Constants.StandardRadioButtonNoValue =>
            keyStoreConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow,
              routes.SubsidiariesSpendingInvestmentController.show().toString())
            Future.successful(Redirect(routes.InvestmentGrowController.show()))
        }
      }
    )
  }
}
