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
import models.NatureOfBusinessModel
import common._
import forms.NatureOfBusinessForm._
import scala.concurrent.Future
import controllers.predicates.ValidActiveSession
import views.html.companyDetails.NatureOfBusiness

object NatureOfBusinessController extends NatureOfBusinessController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait NatureOfBusinessController extends FrontendController with ValidActiveSession{

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness).map {
      case Some(data) => Ok(NatureOfBusiness(natureOfBusinessForm.fill(data)))
      case None => Ok(NatureOfBusiness(natureOfBusinessForm))
    }
  }

  val submit = Action.async { implicit request =>
    natureOfBusinessForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(NatureOfBusiness(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.natureOfBusiness, validFormData)
        Future.successful(Redirect(routes.CommercialSaleController.show()))
      }
    )
  }
}
