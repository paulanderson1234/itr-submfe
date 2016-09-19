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
import controllers.Helpers.KnowledgeIntensiveHelper
import forms.DateOfIncorporationForm._
import models.DateOfIncorporationModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.companyDetails.DateOfIncorporation

import scala.concurrent.Future


object DateOfIncorporationController extends DateOfIncorporationController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
}

trait DateOfIncorporationController extends FrontendController with AuthorisedForTAVC {
  val keyStoreConnector: KeystoreConnector

  val show = Authorised.async { implicit user => implicit request =>
    keyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation).map {
      case Some(data) => Ok(DateOfIncorporation(dateOfIncorporationForm.fill(data)))
      case None => Ok(DateOfIncorporation(dateOfIncorporationForm))
    }
  }

  val submit = Authorised.async { implicit user => implicit request =>
    dateOfIncorporationForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(DateOfIncorporation(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.dateOfIncorporation, validFormData)
        KnowledgeIntensiveHelper.setKiDateCondition(keyStoreConnector, validFormData.day.get, validFormData.month.get, validFormData.year.get)
        Future.successful(Redirect(routes.NatureOfBusinessController.show))
      }
    )
  }
}
