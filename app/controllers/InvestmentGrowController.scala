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
import connectors.{EnrolmentConnector, S4LConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import models._
import common._
import controllers.Helpers.ControllerHelpers
import forms.InvestmentGrowForm._
import play.api.data.Form
import play.api.mvc._

import scala.concurrent.Future
import views.html.investment.InvestmentGrow

object InvestmentGrowController extends InvestmentGrowController
{
  val s4lConnector: S4LConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait InvestmentGrowController extends FrontendController with AuthorisedAndEnrolledForTAVC{

  val s4lConnector: S4LConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(backUrl: Option[String]) = {
      if(backUrl.isDefined) {
        s4lConnector.fetchAndGetFormData[InvestmentGrowModel](KeystoreKeys.investmentGrow).flatMap {
          case Some(data) => getResponse(Ok,investmentGrowForm.fill(data), backUrl.get)
          case None => getResponse(Ok,investmentGrowForm, backUrl.get)
        }
      }
      else Future.successful(Redirect(routes.WhatWillUseForController.show()))
    }

    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkInvestmentGrow, s4lConnector)(hc)
      route <- routeRequest(link)
    } yield route
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    investmentGrowForm.bindFromRequest.fold(
      invalidForm =>
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkInvestmentGrow, s4lConnector)(hc).flatMap {
          case Some(data) => getResponse(BadRequest,invalidForm, data)
          case None => Future.successful(Redirect(routes.WhatWillUseForController.show()))
        },
      validForm => {
        s4lConnector.saveFormData(KeystoreKeys.investmentGrow, validForm)
        Future.successful(Redirect(routes.ContactDetailsController.show()))
      }
    )
  }

  private def getResponse(status: Status, investmentGrowForm: Form[InvestmentGrowModel], backUrl: String)(implicit request: Request[Any]): Future[Result] = {

    def determineResult(newGeographicalMarketModel: Option[NewGeographicalMarketModel],
                        newProductModel: Option[NewProductModel]): Future[Result] = {
      (newGeographicalMarketModel.isDefined,newProductModel.isDefined) match {
        case (true,true) => {
          val hasGeoMarket = newGeographicalMarketModel.get.isNewGeographicalMarket.equals(Constants.StandardRadioButtonYesValue)
          val hasNewProduct = newProductModel.get.isNewProduct.equals(Constants.StandardRadioButtonYesValue)
          Future.successful(status(InvestmentGrow(investmentGrowForm, backUrl,hasGeoMarket,hasNewProduct)))
        }
        case(false,false) => Future.successful(status(InvestmentGrow(investmentGrowForm, backUrl,hasGeoMarket = false,hasNewProduct = false)))
        case(true,false) => Future.successful(Redirect(routes.NewProductController.show()))
        case(false,true) => Future.successful(Redirect(routes.NewGeographicalMarketController.show()))
      }
    }

    for {
      geographicMarket <- s4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket)
      newProduct <- s4lConnector.fetchAndGetFormData[NewProductModel](KeystoreKeys.newProduct)
      result <- determineResult(geographicMarket,newProduct)
    } yield result
  }


}
