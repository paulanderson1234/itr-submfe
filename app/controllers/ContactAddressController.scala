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
import forms.ContactAddressForm._
import models.ContactAddressModel
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html._

import scala.concurrent.Future

object ContactAddressController extends ContactAddressController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait ContactAddressController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[ContactAddressModel](KeystoreKeys.contactAddress).map {
      case Some(data) => Ok(contactInformation.ContactAddress(contactAddressForm.fill(data)))
      case None => Ok(contactInformation.ContactAddress(contactAddressForm))
    }
  }

  val submit = Action.async { implicit request =>
    val response = contactAddressForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(contactInformation.ContactAddress(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.contactAddress, validFormData)
        Redirect(routes.ContactAddressController.show)
      }
    )
    Future.successful(response)
  }
}
