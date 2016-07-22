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

import common.KeystoreKeys
import connectors.KeystoreConnector

import controllers.predicates.ValidActiveSession
import forms.SubsidiariesSpendingInvestmentForm._
import models.SubsidiariesSpendingInvestmentModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc.Action
import views.html._

import scala.concurrent.Future

object SubsidiariesSpendingInvestmentController extends SubsidiariesSpendingInvestmentController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait SubsidiariesSpendingInvestmentController extends FrontendController with ValidActiveSession{

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment).map{
      case Some(data) => Ok(investment.SubsidiariesSpendingInvestment(subsidiariesSpendingInvestmentForm.fill(data)))
      case None => Ok(investment.SubsidiariesSpendingInvestment(subsidiariesSpendingInvestmentForm))
    }
  }

  val submit = Action.async { implicit request =>
    val response = subsidiariesSpendingInvestmentForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(investment.SubsidiariesSpendingInvestment(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.subsidiariesSpendingInvestment, validFormData)
        validFormData.subSpendingInvestment match {
          case "Yes"  => Redirect(routes.SubsidiariesSpendingInvestmentController.show)
          case "No"   => Redirect(routes.SubsidiariesSpendingInvestmentController.show)
        }
      }
    )
    Future.successful(response)
  }

}
