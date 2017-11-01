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
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.internal.InternalService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object InternalController extends InternalController{
  override val internalService = InternalService
}


trait InternalController extends FrontendController{

  val internalService: InternalService

  /* TODO Currently stubbed implementation */
  def getApplicationInProgress: Action[AnyContent] = Action.async {
    implicit request =>{

      implicit val hc = HeaderCarrier.fromHeadersAndSession(request.headers)

      FrontendAuthConnector.currentAuthority.flatMap{
        case Some(authority) => {
          internalService.getApplicationInProgress(authority).map{
            case Some(appInProgress) => Ok(Json.toJson(appInProgress))
            case None => Ok(Json.toJson(false))
          }
        }
        case None => {
          Logger.warn(s"[InternalController] [getApplicationInProgress] - Can't get the Authority")
          Future.successful(Unauthorized)
        }
      }
    }
  }

}