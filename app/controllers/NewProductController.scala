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
import forms.NewProductForm._
import models.{NewProductModel, SubsidiariesModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._

import scala.concurrent.Future
import views.html.investment.NewProduct

object NewProductController extends NewProductController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait NewProductController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[NewProductModel](KeystoreKeys.newProduct).map {
      case Some(data) => Ok(NewProduct(newProductForm.fill(data)))
      case None => Ok(NewProduct(newProductForm))
    }
  }

  val submit = Action.async { implicit request =>

    def routeRequest(date: Option[SubsidiariesModel]): Future[Result] = {
      date match {
        case Some(data) if data.ownSubsidiaries == Constants.StandardRadioButtonYesValue =>
          keyStoreConnector.saveFormData(KeystoreKeys.backLinkSubSpendingInvestment, routes.NewProductController.show().toString())
          Future.successful(Redirect(routes.SubsidiariesSpendingInvestmentController.show()))
        case Some(_) =>
          keyStoreConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow, routes.NewProductController.show().toString())
          Future.successful(Redirect(routes.InvestmentGrowController.show()))
        case None => Future.successful(Redirect(routes.SubsidiariesController.show()))
      }
    }

    newProductForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(NewProduct(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.newProduct, validFormData)
        validFormData.isNewProduct match {
          // TODO: Uncomment below with correct behaviour for 'No' error once decided
          // i.e. replaces existing case Constants.StandardRadioButtonNoValue with line below
          //case Constants.StandardRadioButtonNoValue => Future.successful(Redirect(routes.TOBeDecidedController.show))
          case Constants.StandardRadioButtonNoValue => for {
            date <- keyStoreConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
            route <- routeRequest(date)
          } yield route
          case _ => for {
            date <- keyStoreConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
            route <- routeRequest(date)
          } yield route
        }
      }
    )
  }
}
