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

import auth.AuthorisedAndEnrolledForTAVC
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import forms.SubsidiariesSpendingInvestmentForm._
import models.SubsidiariesSpendingInvestmentModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html._

import scala.concurrent.Future

object SubsidiariesSpendingInvestmentController extends SubsidiariesSpendingInvestmentController{
  val s4lConnector: S4LConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait SubsidiariesSpendingInvestmentController extends FrontendController with AuthorisedAndEnrolledForTAVC{

  val s4lConnector: S4LConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    def routeRequest(backUrl: Option[String]) = {
      if(backUrl.isDefined) {
        s4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment).map {
          case Some(data) => Ok(investment.SubsidiariesSpendingInvestment(subsidiariesSpendingInvestmentForm.fill(data), backUrl.get))
          case None => Ok(investment.SubsidiariesSpendingInvestment(subsidiariesSpendingInvestmentForm, backUrl.get))
        }
      }
      else Future.successful(Redirect(routes.WhatWillUseForController.show()))
    }
    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSubSpendingInvestment, s4lConnector)
      route <- routeRequest(link)
    } yield route
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    subsidiariesSpendingInvestmentForm.bindFromRequest.fold(
      invalidForm =>
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSubSpendingInvestment, s4lConnector).flatMap {
          case Some(data) => Future.successful(
            BadRequest(views.html.investment.SubsidiariesSpendingInvestment(invalidForm, data)))
          case None => Future.successful(Redirect(routes.WhatWillUseForController.show()))
      },
      validForm => {
        s4lConnector.saveFormData(KeystoreKeys.subsidiariesSpendingInvestment, validForm)
        validForm.subSpendingInvestment match {
          case  Constants.StandardRadioButtonYesValue  =>
            Future.successful(Redirect(routes.SubsidiariesNinetyOwnedController.show()))
          case  Constants.StandardRadioButtonNoValue =>
            s4lConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow,
              routes.SubsidiariesSpendingInvestmentController.show().toString())
            Future.successful(Redirect(routes.InvestmentGrowController.show()))
        }
      }
    )
  }
}
