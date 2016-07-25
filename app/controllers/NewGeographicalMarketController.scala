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

import common.KeystoreKeys
import connectors.KeystoreConnector
import controllers.predicates.ValidActiveSession
import forms.NewGeographicalMarketForm._
import models.NewGeographicalMarketModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import views.html.investment.NewGeographicalMarket

object NewGeographicalMarketController extends NewGeographicalMarketController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait NewGeographicalMarketController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    def routeRequest(backUrl: String) = {
      keyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket) map {
        case Some(data) => Ok(NewGeographicalMarket(newGeographicalMarketForm.fill(data), backUrl))
        case None => Ok(NewGeographicalMarket(newGeographicalMarketForm, backUrl))
      }
    }

    for {
      link <- getBackLink
      route <- routeRequest(link)
    } yield route
  }

  val submit = Action.async { implicit request =>
    newGeographicalMarketForm.bindFromRequest.fold(
      invalidForm => getBackLink.flatMap(url => Future.successful(BadRequest(NewGeographicalMarket(invalidForm, url)))),
      validForm => {
        keyStoreConnector.saveFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket, validForm)
        Future.successful(Redirect(routes.NewProductController.show()))
      }
    )
  }

  def getBackLink(implicit hc: HeaderCarrier): Future[String] = {
    //TODO: this needs to reuse logic that determines the forward navigation (3 possible routes)
    Future.successful(routes.ProposedInvestmentController.show.toString())
  }
}
