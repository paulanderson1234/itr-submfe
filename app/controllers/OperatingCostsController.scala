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
import forms.OperatingCostsForm._
import models.OperatingCostsModel
import play.api.mvc.Action
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.knowledgeIntensive.OperatingCosts

import scala.concurrent.Future


object OperatingCostsController extends OperatingCostsController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait OperatingCostsController extends FrontendController with ValidActiveSession{
  val keyStoreConnector: KeystoreConnector
  val show = ValidateSession.async { implicit request =>
   keyStoreConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts).map {
     case Some(data) => Ok(OperatingCosts(operatingCostsForm.fill(data)))
     case None => Ok(OperatingCosts(operatingCostsForm))
   }
  }

  val submit = Action.async { implicit request =>
    val response = operatingCostsForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(OperatingCosts(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.operatingCosts, validFormData)
        Redirect(routes.PercentageStaffWithMastersController.show())
      }
    )
    Future.successful(response)
  }
}
