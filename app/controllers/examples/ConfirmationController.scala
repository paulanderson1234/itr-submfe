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

package controllers.examples

import connectors.KeystoreConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import models.DoSubmissionModel
import common._
import views.html._
import controllers.predicates.ValidActiveSession
import scala.concurrent.Future

object ConfirmationController extends ConfirmationController

trait ConfirmationController extends FrontendController with ValidActiveSession{

  val show = ValidateSession.async { implicit request =>
      Future.successful(Ok(views.html.examples.Confirmation()))
    }
}
