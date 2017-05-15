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

import testOnly.connectors.ResetTokenConnector
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController

object ResetTokenController extends ResetTokenController {
  override lazy val resetTokenConnector = ResetTokenConnector
}

trait ResetTokenController extends FrontendController  {

  val resetTokenConnector: ResetTokenConnector

  def resetTokens(): Action[AnyContent] = Action.async { implicit request =>
    resetTokenConnector.resetTokens().map {
      response => response.status match {
        case OK => Ok("Successfully reset tokens")
        case _ => BadRequest("Failed to reset tokens")
      }
    }
  }

}