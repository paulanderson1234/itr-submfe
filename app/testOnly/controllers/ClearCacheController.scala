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

package testOnly.controllers

import auth.AuthorisedAndEnrolledForTAVC
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController

object ClearCacheController extends ClearCacheController {
  override lazy val s4lConnector = S4LConnector
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig = FrontendAppConfig
}

trait ClearCacheController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq()

  val s4lConnector: S4LConnector

  def clearCache(): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.clearCache().map {
      response => response.status match {
        case NO_CONTENT => Ok("Successfully cleared cache")
        case _ => BadRequest("Failed to clear cache")
      }
    }
  }

}
