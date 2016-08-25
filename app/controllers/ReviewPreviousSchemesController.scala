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
import controllers.Helpers.ControllerHelpers
import controllers.predicates.ValidActiveSession
import models.PreviousSchemeModel
import play.api.mvc.Action
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.checkAndSubmit.CheckAnswers
import views.html.introduction.start
import views.html.previousInvestment.ReviewPreviousSchemes

import scala.concurrent.Future

object ReviewPreviousSchemesController extends ReviewPreviousSchemesController {
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait ReviewPreviousSchemesController extends FrontendController with ValidActiveSession{

  val keyStoreConnector: KeystoreConnector
  def previousSchemes(implicit headerCarrier: HeaderCarrier) : Future[Vector[PreviousSchemeModel]] =
    ControllerHelpers.getAllInvestmentFromKeystore(keyStoreConnector)

  val show = ValidateSession.async { implicit request =>
    previousSchemes.flatMap(previousSchemes =>
      Future.successful(Ok(ReviewPreviousSchemes(previousSchemes))))
  }

  val submit = Action.async { implicit request =>
    previousSchemes.flatMap(previousSchemes =>
      if(!previousSchemes.isEmpty) Future.successful(Redirect(routes.ProposedInvestmentController.show()))
      else Future.successful(Redirect(routes.ReviewPreviousSchemesController.show())))
  }
}
