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

package auth

import auth.authModels.UserIDs
import config.FrontendGlobal.internalServerErrorTemplate
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.play.frontend.auth.{Actions, AuthContext}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait FrontendAuthorisedForTAVC extends Actions {

  private type PlayRequest = Request[AnyContent] => Result
  private type UserRequest = UserIDs => PlayRequest
  private type AsyncPlayRequest = Request[AnyContent] => Future[Result]
  private type AsyncUserRequest = UserIDs => AsyncPlayRequest

  class FrontendAuthorised {
    def async(action: AsyncUserRequest): Action[AnyContent] = {
      Action.async {
        implicit request =>
          implicit val hc = HeaderCarrier.fromHeadersAndSession(request.headers)
          authConnector.currentAuthority.flatMap {
            case Some(authority) => {
              authConnector.getIds[UserIDs](AuthContext(authority)).flatMap {
                userIDs => {
                  action(userIDs)(request)
                }
              }
            }
            case None => {
              Logger.error(s"[FrontendAuthorised] - No Authority record found")
              Future.successful(Unauthorized)
            }
          } recover {
            case ex: Exception =>
              Logger.error(s"[FrontendAuthorised] - Received an error when retrieving Authority - error: ${ex.getMessage}")
              InternalServerError(internalServerErrorTemplate)
          }
      }
    }

    def apply(action: UserRequest): Action[AnyContent] = async(userIDs => request => Future.successful(action(userIDs)(request)))
  }

  object FrontendAuthorised extends FrontendAuthorised

}

