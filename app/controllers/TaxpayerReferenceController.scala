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
import models.TaxpayerReferenceModel
import common._
import forms.TaxPayerReferenceForm._

import scala.concurrent.Future
import controllers.predicates.ValidActiveSession
import views.html.companyDetails.TaxpayerReference

object TaxpayerReferenceController extends TaxpayerReferenceController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait TaxpayerReferenceController extends FrontendController with ValidActiveSession{

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[TaxpayerReferenceModel](KeystoreKeys.taxpayerReference).map {
      case Some(data) => Ok(TaxpayerReference(taxPayerReferenceForm.fill(data)))
      case None => Ok(TaxpayerReference(taxPayerReferenceForm))
    }
  }

  val submit = Action.async { implicit request =>
    taxPayerReferenceForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(TaxpayerReference(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.taxpayerReference, validFormData)
        Future.successful(Redirect(routes.RegisteredAddressController.show))
      }
    )
  }
}
