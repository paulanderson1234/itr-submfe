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
import forms.ConfirmCorrespondAddressForm._
import models.ConfirmCorrespondAddressModel
import play.api.mvc.Action
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.contactInformation.ConfirmCorrespondAddress

import scala.concurrent.Future

object ConfirmCorrespondAddressController extends ConfirmCorrespondAddressController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait ConfirmCorrespondAddressController extends FrontendController with ValidActiveSession{

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async{ implicit request =>
    keyStoreConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](KeystoreKeys.confirmContactAddress).map {
      case Some(data) => Ok(ConfirmCorrespondAddress(confirmCorrespondAddressForm.fill(data)))
      case None => Ok(ConfirmCorrespondAddress(confirmCorrespondAddressForm))
    }
  }

  val submit = Action.async{ implicit  request =>
    val response = confirmCorrespondAddressForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(ConfirmCorrespondAddress(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.confirmContactAddress, validFormData)
        Redirect(routes.ConfirmCorrespondAddressController.show())
      }
    )
    Future.successful(response)
  }
}
