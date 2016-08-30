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
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object LifetimeAllowanceExceededController extends LifetimeAllowanceExceededController {
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait LifetimeAllowanceExceededController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    Future.successful(Ok(views.html.investment.LifetimeAllowanceExceeded()))
  }

  val submit = Action.async { implicit request =>
    keyStoreConnector.saveFormData(KeystoreKeys.backLinkProposedInvestment, routes.ReviewPreviousSchemesController.show().toString())
    Future.successful(Redirect(routes.ProposedInvestmentController.show()))
  }
}
