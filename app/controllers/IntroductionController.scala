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

import java.util.UUID
import connectors.KeystoreConnector
import controllers.predicates.ValidActiveSession
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.http.logging.SessionId
import play.api.mvc._


import scala.concurrent.Future
import views.html.introduction._

object IntroductionController extends IntroductionController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait IntroductionController extends FrontendController with ValidActiveSession{

  implicit val hc = new HeaderCarrier()
  val keystoreConnector : KeystoreConnector = KeystoreConnector

  // this is the page that is called on a restart. It will populate the session keys if missing.
  val show = Action.async { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString

      //TODO: if there is any data (i.e. a model passed to this form create an empty model and pass it..
      Future.successful(Redirect(routes.IntroductionController.show())
        .withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      //TODO: if there is any data (i.e. a model passed to this form) retrive from session skeystore (fetchAndGet)
      // and pass it to the form..
      Future.successful(Ok(Introduction()))
      }
    }

  val submit = Action.async { implicit request =>
    Future.successful(Ok("the form was posted successfully"))
  }

  // this method is called on any restart - e.g. on session timeout
  def restart(): Action[AnyContent] = Action.async { implicit request =>
    keystoreConnector.clearKeystore()
    Future.successful(Redirect(routes.StartController.start()))
  }
}
