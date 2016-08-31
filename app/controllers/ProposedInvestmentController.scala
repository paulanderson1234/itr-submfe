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

import connectors.KeystoreConnector
import controllers.Helpers.ControllerHelpers
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import models.ProposedInvestmentModel
import common._
import forms.ProposedInvestmentForm._

import scala.concurrent.Future
import controllers.predicates.ValidActiveSession
import views.html.investment.ProposedInvestment

object ProposedInvestmentController extends ProposedInvestmentController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait ProposedInvestmentController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show: Action[AnyContent] = ValidateSession.async { implicit request =>
    def routeRequest(backUrl: Option[String]) = {
      if (backUrl.isDefined) {
        keyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel](KeystoreKeys.proposedInvestment).map {
          case Some(data) => Ok(ProposedInvestment(proposedInvestmentForm.fill(data), backUrl.get))
          case None => Ok(ProposedInvestment(proposedInvestmentForm, backUrl.get))
        }

      } else {
        // no back link - send to beginning of flow
        Future.successful(Redirect(routes.HadPreviousRFIController.show()))
      }
    }
    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkProposedInvestment, keyStoreConnector)(hc)
      route <- routeRequest(link)
    } yield route
  }

  val submit = Action.async { implicit request =>
    proposedInvestmentForm.bindFromRequest().fold(
      formWithErrors => {
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkProposedInvestment, keyStoreConnector).flatMap(url =>
          Future.successful(BadRequest(ProposedInvestment(formWithErrors, url.getOrElse(routes.HadPreviousRFIController.show().toString)))))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.proposedInvestment, validFormData)
        //TODO: needs to go to what will use investment for page
        Future.successful(Redirect(routes.WhatWillUseForController.show()))
      }
    )
  }

}
