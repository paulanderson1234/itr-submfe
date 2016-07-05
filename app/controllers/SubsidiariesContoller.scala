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
import models.{SubsidiariesModel}
import forms.SubsidiariesForm._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import scala.concurrent.Future
import views.html._

object SubsidiariesController extends SubsidiariesController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait SubsidiariesController extends FrontendController with ValidActiveSession{

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries).map {
      case Some(data) => Ok(companyDetails.Subsidiaries(subsidiariesForm.fill(data)))
      case None => Ok(companyDetails.Subsidiaries(subsidiariesForm))
    }
  }

  val submit = Action.async { implicit request =>
    val response = subsidiariesForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(companyDetails.Subsidiaries(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.subsidiaries, validFormData)
        Redirect(routes.SubsidiariesController.show())
      }
    )
    Future.successful(response)
  }
}
