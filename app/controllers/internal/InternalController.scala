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

package controllers.internal
import auth.AuthorisedAndEnrolledForTAVC
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object InternalController extends InternalController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}


trait InternalController extends FrontendController with AuthorisedAndEnrolledForTAVC{

  val s4lConnector: S4LConnector

  override lazy val acceptedFlows = Seq()

  /* TODO Currently stubbed implementation */
  def getApplicationInProgress(internalId: String): Action[AnyContent] = Action.async {
    implicit request =>{
      Future.successful(Ok(Json.toJson(false)))
    }
  }

}