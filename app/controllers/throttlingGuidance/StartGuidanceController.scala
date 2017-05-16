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

package controllers.throttlingGuidance

import java.util.UUID

import play.api.mvc.{Action, AnyContent, Request}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

object StartGuidanceController extends StartGuidanceController

trait StartGuidanceController extends FrontendController {




  def start:Action[AnyContent] = Action.async { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {

      implicit val hc = new HeaderCarrier()

      val sessionId = UUID.randomUUID.toString

      //val d  = Some(uk.gov.hmrc.play.http.logging.SessionId(s"session-$sessionId"))

      //implicit val hc = new HeaderCarrier(sessionId =  Some(uk.gov.hmrc.play.http.logging.SessionId(s"session-$sessionId")))

//      implicit  def hc(implicit request: Request[_]): HeaderCarrier = {
//
//        //TODO: remove - just for debugging issue ========================
//        if (request.session.get(SessionKeys.sessionId).isEmpty) {
//          println("==================================SESSION ID IS EMPTY===================================")
//        } else {
//          println(s"==================================SESSION NOT IS EMPTY============ id is: ${request.session.get(SessionKeys.sessionId)}")
//        }
//
//        val f = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))
//        println(s"============================== in implict hc uathfor/tavc. session id is  ${f.sessionId}    ==========================")
//        f
//      }

      //TODO: end remove========================================================================================================

      Future.successful(Redirect(routes.FirstTimeUsingServiceController.show()).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      Future.successful(Redirect(routes.FirstTimeUsingServiceController.show()))

    }
  }

}
