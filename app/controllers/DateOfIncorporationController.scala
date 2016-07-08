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
import forms.DateOfIncorporationForm._
import models.DateOfIncorporationModel
import play.api.mvc.Action
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.companyDetails.DateOfIncorporation

import scala.concurrent.Future


object DateOfIncorporationController extends DateOfIncorporationController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait DateOfIncorporationController extends FrontendController with ValidActiveSession {
  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation).map {
      case Some(data) => Ok(DateOfIncorporation(dateOfIncorporationForm.fill(data)))
      case None => Ok(DateOfIncorporation(dateOfIncorporationForm))
    }
  }

  val submit = Action.async { implicit request =>
    val response = dateOfIncorporationForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(DateOfIncorporation(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.dateOfIncorporation, validFormData)
        Redirect(routes.NatureOfBusinessController.show)
      }
    )
    Future.successful(response)
  }
}

