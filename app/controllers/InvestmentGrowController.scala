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

import auth.AuthorisedAndEnrolledForTAVC
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, KeystoreConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import models._
import common._
import controllers.Helpers.ControllerHelpers
import forms.InvestmentGrowForm._

import scala.concurrent.Future
import views.html.investment.InvestmentGrow

object InvestmentGrowController extends InvestmentGrowController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait InvestmentGrowController extends FrontendController with AuthorisedAndEnrolledForTAVC{

  val keyStoreConnector: KeystoreConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(backUrl: Option[String]) = {
      if(backUrl.isDefined) {
        keyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](KeystoreKeys.investmentGrow).map {
          case Some(data) => Ok(InvestmentGrow(investmentGrowForm.fill(data), backUrl.get))
          case None => Ok(InvestmentGrow(investmentGrowForm, backUrl.get))
        }
      }
      else Future.successful(Redirect(routes.ProposedInvestmentController.show()))
    }

    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkInvestmentGrow, keyStoreConnector)(hc)
      route <- routeRequest(link)
    } yield route
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    investmentGrowForm.bindFromRequest.fold(
      invalidForm =>
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkInvestmentGrow, keyStoreConnector)(hc).flatMap {
          case Some(data) => Future.successful(BadRequest(views.html.investment.InvestmentGrow(invalidForm, data)))
          case None => Future.successful(Redirect(routes.ProposedInvestmentController.show()))
        },
      validForm => {
        keyStoreConnector.saveFormData(KeystoreKeys.investmentGrow, validForm)
        Future.successful(Redirect(routes.ContactDetailsController.show()))
      }
    )
  }
}
