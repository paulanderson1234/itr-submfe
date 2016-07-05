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
import forms.RegisteredAddressForm._
import models.RegisteredAddressModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import views.html._
import scala.concurrent.Future

object RegisteredAddressController extends RegisteredAddressController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait RegisteredAddressController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[RegisteredAddressModel](KeystoreKeys.registeredAddress).map {
      case Some(data) => Ok(companyDetails.RegisteredAddress(registeredAddressForm.fill(data)))
      case None => Ok(companyDetails.RegisteredAddress(registeredAddressForm))
    }
  }

  val submit = Action.async { implicit request =>
    val response = registeredAddressForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(companyDetails.RegisteredAddress(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.registeredAddress, validFormData)
        Redirect(routes.RegisteredAddressController.show)
      }
    )
    Future.successful(response)
  }
}
