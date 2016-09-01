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

import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
import controllers.predicates.ValidActiveSession
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import views.html.knowledgeIntensive.IneligibleForKI
import controllers.Helpers.ControllerHelpers

import scala.concurrent.Future
import views.html._

object IneligibleForKIController extends IneligibleForKIController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait IneligibleForKIController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    def routeRequest(backUrl: Option[String]) = {
      if (backUrl.isDefined) {
        Future.successful(Ok(IneligibleForKI(backUrl.get)))
      } else {
        // no back link - send back to same page
        //Future.successful(Ok(IneligibleForKI("")))
        Future.successful(Redirect(routes.OperatingCostsController.show()))
      }
    }
    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkIneligibleForKI, keyStoreConnector)(hc)
      route <- routeRequest(link)
    } yield route
  }

  val submit = Action.async { implicit request => {
    keyStoreConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries, routes.IneligibleForKIController.show().toString())
    Future.successful(Redirect(routes.SubsidiariesController.show()))
    }
  }

}
