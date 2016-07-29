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
import models.InvestmentGrowModel
import common._
import forms.InvestmentGrowForm._

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
    keyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](KeystoreKeys.investmentGrow).map {
      case Some(data) => Ok(InvestmentGrow(investmentGrowForm.fill(data)))
      case None => Ok(InvestmentGrow(investmentGrowForm))
    }
  }

  val submit = Action.async { implicit request =>
    val response = investmentGrowForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(InvestmentGrow(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.investmentGrow, validFormData)
        Redirect(routes.InvestmentGrowController.show())
      }
    )
    Future.successful(response)
  }
}
