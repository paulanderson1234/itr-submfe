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
import auth.FrontendAuthorisedForTAVC
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.S4LConnector
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object InternalController extends InternalController{
  override val authConnector = FrontendAuthConnector
  override val s4LConnector = S4LConnector
}


trait InternalController extends FrontendController with FrontendAuthorisedForTAVC{

  val s4LConnector: S4LConnector

  def getApplicationInProgress: Action[AnyContent] = FrontendAuthorised.async {
    implicit userIds => implicit request =>{
      s4LConnector.fetchAndGetFormData[Boolean](userIds.internalId, KeystoreKeys.applicationInProgress).map{
        case Some(appInProgress) => Ok(Json.toJson(appInProgress))
        case None => Ok(Json.toJson(false))
      }
    }
  }

}