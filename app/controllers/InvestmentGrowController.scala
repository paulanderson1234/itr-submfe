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
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import models._
import common._
import forms.InvestmentGrowForm._
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html._
import scala.concurrent.Future
import controllers.predicates.ValidActiveSession
import views.html.investment.InvestmentGrow

object InvestmentGrowController extends InvestmentGrowController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait InvestmentGrowController extends FrontendController with ValidActiveSession{

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String) = {
      keyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](KeystoreKeys.investmentGrow).map {
        case Some(data) => Ok(InvestmentGrow(investmentGrowForm.fill(data),backUrl))
        case None => Ok(InvestmentGrow(investmentGrowForm,backUrl))
      }
    }

    for {
      link <- getBackLink
      route <- routeRequest(link)
    } yield route
  }

  val submit = Action.async { implicit request =>
    investmentGrowForm.bindFromRequest.fold(
      invalidForm => getBackLink.flatMap(url => Future.successful(BadRequest(investment.InvestmentGrow(invalidForm, url)))),
      validForm => {
        keyStoreConnector.saveFormData(KeystoreKeys.subsidiariesSpendingInvestment, validForm)
        Future.successful(Redirect(routes.InvestmentGrowController.show()))
      }
    )
  }


  def getBackLink(implicit hc: HeaderCarrier): Future[String] = {
    def routeRequest(newGeographicalMarket: Option[NewGeographicalMarketModel], subsidiariesSpendingInvestment: Option[SubsidiariesSpendingInvestmentModel],
                     newProduct: Option[NewProductModel], previousBeforeDOFCS : Option[PreviousBeforeDOFCSModel],
                     whatWillUseFor: Option[WhatWillUseForModel]): String = {
      (newGeographicalMarket,subsidiariesSpendingInvestment,newProduct,previousBeforeDOFCS,whatWillUseFor) match {
        case (Some(newGeographicalMarket),_,_,_,_) => routes.NewGeographicalMarketController.show.toString()
        case (None,Some(subsidiariesInvestment),_,_,_) => routes.SubsidiariesSpendingInvestmentController.show.toString()
        case (None,None,Some(newProduct),_,_) => routes.NewProductController.show.toString()
        case (None,None,None,Some(previousBeforeDOFCS),_) => routes.PreviousBeforeDOFCSController.show.toString()
        case (None,None, None,None, Some(whatWillUseFor)) => routes.WhatWillUseForController.show.toString()
        case _ => routes.WhatWillUseForController.show.toString()
      }
    }


    // todo change newGeographicMarket to subsidiaries90Owned when it is created
    for {
      newGeographicalMarket <- keyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket)
      subsidiariesSpendingInvestment <- keyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment)
      newProduct <- keyStoreConnector.fetchAndGetFormData[NewProductModel](KeystoreKeys.newProduct)
      previousBeforeDOFCS <- keyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS)
      whatWillUseFor<- keyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](KeystoreKeys.whatWillUseFor)
      route <- Future.successful(routeRequest(newGeographicalMarket,subsidiariesSpendingInvestment,newProduct,previousBeforeDOFCS,whatWillUseFor))
    } yield route

  }
}
