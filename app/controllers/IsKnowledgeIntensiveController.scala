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
import forms.IsKnowledgeIntensiveForm._
import models.IsKnowledgeIntensiveModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import scala.concurrent.Future
import views.html._

object IsKnowledgeIntensiveController extends IsKnowledgeIntensiveController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait IsKnowledgeIntensiveController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](KeystoreKeys.isKnowledgeIntensive).map {
      case Some(data) => Ok(companyDetails.IsKnowledgeIntensive(isKnowledgeIntensiveForm.fill(data)))
      case None => Ok(companyDetails.IsKnowledgeIntensive(isKnowledgeIntensiveForm))
    }
  }

  val submit = Action.async { implicit request =>
    val response = isKnowledgeIntensiveForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(companyDetails.IsKnowledgeIntensive(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.isKnowledgeIntensive, validFormData)
        Redirect(routes.SubsidiariesController.show)
      }
    )
    Future.successful(response)
  }
}
