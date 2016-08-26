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
    def routeRequest(backUrl: Option[String]) = {
      if (backUrl.isDefined) {
        id match {
          case Some(id) => {
            ControllerHelpers.getExistingInvestmentFromKeystore(keyStoreConnector, id).map {
              case Some(data) => Ok(PreviousScheme(previousSchemeForm.fill(data), backUrl.get))
              case None => Ok(PreviousScheme(previousSchemeForm, backUrl.get))
            }
          }
          case None => {
            Future.successful(Ok(PreviousScheme(previousSchemeForm, backUrl.get)))
          }
        }
      } else {
        // no back link - send to beginning of flow
        Future.successful(Redirect(routes.HadPreviousRFIController.show()))
      }
    }
    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkPreviousScheme, keyStoreConnector)(hc)
      route <- routeRequest(link)
    } yield route
  }

  val submit = Action.async { implicit request =>
    previousSchemeForm.bindFromRequest().fold(
      formWithErrors => {
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkPreviousScheme, keyStoreConnector).flatMap(url =>
          Future.successful(BadRequest(PreviousScheme(formWithErrors, url.get))))
      },
      validFormData => {
        validFormData.processingId match {
          case Some(id) => ControllerHelpers.updateKeystorePreviousInvestment(keyStoreConnector, validFormData).map {
            _ => Redirect(routes.ReviewPreviousSchemesController.show())
          }
          case None => ControllerHelpers.addPreviousInvestmentToKeystore(keyStoreConnector, validFormData).map {
            _ => Redirect(routes.ReviewPreviousSchemesController.show())
          }
        }
      }
    )
  }

}
