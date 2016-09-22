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

import auth.AuthorisedForTAVC
import common.KeystoreKeys
import config.{FrontendAuthConnector, FrontendAppConfig}
import connectors.KeystoreConnector
import forms.SubsidiariesNinetyOwnedForm._
import models.SubsidiariesNinetyOwnedModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.investment.SubsidiariesNinetyOwned

import scala.concurrent.Future

object SubsidiariesNinetyOwnedController extends SubsidiariesNinetyOwnedController  {
  val keyStoreConnector: KeystoreConnector =  KeystoreConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
}

trait SubsidiariesNinetyOwnedController extends FrontendController with AuthorisedForTAVC {

  val keyStoreConnector: KeystoreConnector

  val show = Authorised.async { implicit user => implicit request =>
    keyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](KeystoreKeys.subsidiariesNinetyOwned).map {
      case Some(data) => Ok(SubsidiariesNinetyOwned(subsidiariesNinetyOwnedForm.fill(data)))
      case None => Ok(SubsidiariesNinetyOwned(subsidiariesNinetyOwnedForm))
    }
  }

  val submit = Authorised.async { implicit user => implicit request =>
    subsidiariesNinetyOwnedForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(SubsidiariesNinetyOwned(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.subsidiariesNinetyOwned, validFormData)
        keyStoreConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow, routes.SubsidiariesNinetyOwnedController.show().toString())
        Future.successful(Redirect(routes.InvestmentGrowController.show()))
      }
    )
  }
}
