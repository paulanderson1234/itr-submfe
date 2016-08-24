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
import forms.PreviousSchemeForm
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import controllers.Helpers.ControllerHelpers

import scala.concurrent.Future
import controllers.predicates.ValidActiveSession
import views.html.previousInvestment.PreviousScheme
import forms.PreviousSchemeForm._

object PreviousSchemeController extends PreviousSchemeController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait PreviousSchemeController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  def show(id: Option[Int]): Action[AnyContent] = ValidateSession.async { implicit request =>

    id match {
      case Some(id) => {
        ControllerHelpers.getExistingInvestmentFromKeystore(keyStoreConnector, id).map {
          case Some(data) => Ok(PreviousScheme(previousSchemeForm.fill(data)))
          case None => Ok(PreviousScheme(previousSchemeForm))
        }
      }
      case None => {
        Future.successful(Ok(PreviousScheme(previousSchemeForm)))
      }
    }
  }

  def delete(id: Int): Action[AnyContent] = ValidateSession.async { implicit request =>
    ControllerHelpers.removeKeystorePreviousInvestment(keyStoreConnector, id).map {
      _=> Redirect(routes.ReviewPreviousSchemesController.show())
    }
  }

  val submit = Action.async { implicit request =>
    previousSchemeForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(PreviousScheme(formWithErrors)))
      },
      validFormData => {
        validFormData.processingId match {
          case Some(id) => ControllerHelpers.updateKeystorePreviousInvestment(keyStoreConnector, validFormData).map {
            _ => Redirect(routes.ReviewPreviousSchemesController.show())
          }
          case None => ControllerHelpers.addPreviousInvestmentToKeystore(keyStoreConnector, validFormData).map{
            _ => Redirect(routes.ReviewPreviousSchemesController.show())
          }
        }
      }
    )
  }
}
