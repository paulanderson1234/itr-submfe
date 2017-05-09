/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.throttlingGuidance


import common.KeystoreKeys
import connectors.KeystoreConnector
import controllers.predicates.ValidActiveSession
import models.throttlingGuidance.IsAgentModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.Action
import views.html.throttlingGuidance.IsAgent
import forms.throttlingGuidance.IsAgentForm._

import scala.concurrent.Future


object IsAgentController extends IsAgentController{
  override val keystoreConnector = KeystoreConnector
}

trait IsAgentController extends FrontendController with ValidActiveSession{

  val keystoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keystoreConnector.fetchAndGetFormData[IsAgentModel](KeystoreKeys.isAgent).map {
      case Some(data) => Ok(IsAgent(isAgentForm.fill(data)))
      case _ => Ok(IsAgent(isAgentForm))
    }
  }

  val submit = Action.async { implicit request =>
    isAgentForm.bindFromRequest().fold(
      formWithErrors => {
          Future.successful(BadRequest(IsAgent(formWithErrors)))
      },
      validFormData => {
        keystoreConnector.saveFormData(KeystoreKeys.isAgent, validFormData)
        Future.successful(Redirect(routes.IsAgentController.show()))
      }
    )
  }
}
