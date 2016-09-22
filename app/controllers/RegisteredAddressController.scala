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
import forms.RegisteredAddressForm._
import models.RegisteredAddressModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import views.html._
import scala.concurrent.Future

object RegisteredAddressController extends RegisteredAddressController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
}

trait RegisteredAddressController extends FrontendController with AuthorisedForTAVC {

  val keyStoreConnector: KeystoreConnector

  val show = Authorised.async { implicit user => implicit request =>
    keyStoreConnector.fetchAndGetFormData[RegisteredAddressModel](KeystoreKeys.registeredAddress).map {
      case Some(data) => Ok(companyDetails.RegisteredAddress(registeredAddressForm.fill(data)))
      case None => Ok(companyDetails.RegisteredAddress(registeredAddressForm))
    }
  }

  val submit = Authorised.async { implicit user => implicit request =>
    registeredAddressForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(companyDetails.RegisteredAddress(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.registeredAddress, validFormData)
        Future.successful(Redirect(routes.DateOfIncorporationController.show))
      }
    )
  }
}
