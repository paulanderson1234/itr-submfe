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
import forms.PreviousBeforeDOFCSForm._
import controllers.predicates.ValidActiveSession
import models.PreviousBeforeDOFCSModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import scala.concurrent.Future
import views.html._

object PreviousBeforeDOFCSController extends PreviousBeforeDOFCSController {
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait PreviousBeforeDOFCSController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS).map {
      case Some(data) => Ok(previousInvestment.PreviousBeforeDOFCS(previousBeforeDOFCSForm.fill(data)))
      case None => Ok(previousInvestment.PreviousBeforeDOFCS(previousBeforeDOFCSForm))
    }
  }

  val submit = Action.async { implicit request =>
    val response = previousBeforeDOFCSForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(previousInvestment.PreviousBeforeDOFCS(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.previousBeforeDOFCS, validFormData)
        validFormData.previousBeforeDOFCS match {
          case "Yes"  => Redirect(routes.PreviousBeforeDOFCSController.show)
          case "No"   => Redirect(routes.PreviousBeforeDOFCSController.show)
        }
      }
    )
    Future.successful(response)
  }
}
