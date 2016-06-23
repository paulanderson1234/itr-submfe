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

package controllers.examples

import connectors.KeystoreConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import models.ContactDetailsModel
import common._
import views.html._
import forms.ContactDetailsForm._
import scala.concurrent.Future
import controllers.predicates.ValidActiveSession

object ContactDetailsController extends ContactDetailsController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait ContactDetailsController extends FrontendController with ValidActiveSession{

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetailsExample).map {
      case Some(data) => Ok(examples.ContactDetails(contactDetailsForm.fill(data)))
      case None => Ok(examples.ContactDetails(contactDetailsForm))
    }
  }

  val submit = Action.async { implicit request =>
    val response = contactDetailsForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(examples.ContactDetails(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.contactDetailsExample, validFormData)
        Redirect(routes.DateOfFirstSaleController.show)
      }
    )
    Future.successful(response)
  }
}
