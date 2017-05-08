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

package controllers.throttlingGuidance

import common.{Constants, KeystoreKeys}
import config.{AppConfig, FrontendAppConfig}
import connectors.KeystoreConnector
import forms.FirstTimeUsingServiceForm._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Action
import services.{ThrottleService, TokenService}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.throttlingGuidance.FirstTimeUsingService

import scala.concurrent.Future

object FirstTimeUsingServiceController extends FirstTimeUsingServiceController{
  val keystoreConnector = KeystoreConnector
  val throttleService: ThrottleService = ThrottleService
  val tokenService: TokenService = TokenService
  val applicationConfig = FrontendAppConfig
}

trait FirstTimeUsingServiceController extends FrontendController  {

  val keystoreConnector : KeystoreConnector
  val throttleService: ThrottleService
  val tokenService: TokenService
  val applicationConfig : AppConfig

  val show = Action.async{
    implicit request =>Future.successful(Ok(views.html.throttlingGuidance.FirstTimeUsingService(firstTimeUsingServiceForm)))
  }

  val submit = Action.async {
    implicit request =>
      firstTimeUsingServiceForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(FirstTimeUsingService(formWithErrors)))
        },
        validFormData => {
          keystoreConnector.saveFormData(KeystoreKeys.isFirstTimeUsingService, validFormData)
          validFormData.isFirstTimeUsingService match {
            case Constants.StandardRadioButtonYesValue => {
              throttleService.checkUserAccess.flatMap {
                case true => {
                  tokenService.generateTemporaryToken
                  Future.successful(Redirect(controllers.routes.ApplicationHubController.show()))
                }
                case false => Future.successful(Redirect(controllers.throttlingGuidance.routes.UserLimitReachedController.show()))
              }
            }
            case Constants.StandardRadioButtonNoValue => Future.successful(Redirect(controllers.routes.ApplicationHubController.show()))
          }
        }
      )
  }
}