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
import forms.YourCompanyNeedForm._
import models.YourCompanyNeedModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import views.html._
import scala.concurrent.Future

object YourCompanyNeedController extends YourCompanyNeedController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait YourCompanyNeedController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](KeystoreKeys.yourCompanyNeed).map {
      case Some(data) => Ok(introduction.YourCompanyNeed(yourCompanyNeedForm.fill(data)))
      case None => Ok(introduction.YourCompanyNeed(yourCompanyNeedForm))
    }
  }

  val submit = Action.async { implicit request =>
    yourCompanyNeedForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(introduction.YourCompanyNeed(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.yourCompanyNeed, validFormData)
        Future.successful(Redirect(routes.QualifyingForSchemeController.show()))
      }
    )
  }
}
