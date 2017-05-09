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

package controllers.eligibility

import common.{KeystoreKeys, Constants}
import config.FrontendGlobal._
import connectors.KeystoreConnector
import models.eligibility.{AcquiredTradeEligibilityModel, GroupsAndSubsEligibilityModel}
import play.api.mvc.{Result, Action, AnyContent}
import services.TokenService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eligibility.AcquiredTradeEligibility
import scala.concurrent.Future
import forms.AcquiredTradeEligibilityForm._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object AcquiredTradeEligibilityController extends AcquiredTradeEligibilityController{
  override val keystoreConnector: KeystoreConnector = KeystoreConnector
  override val tokenService: TokenService = TokenService
}

trait AcquiredTradeEligibilityController extends FrontendController{

  val keystoreConnector: KeystoreConnector
  val tokenService: TokenService

  val show: Action[AnyContent] = Action.async { implicit request =>
    //    KeystoreConnector.fetchAndGetFormData[AcquiredTradeEligibilityModel](KeystoreKeys.acquiredTradeEligibility) map {
    //      case Some(data) => Ok(AcquiredTradeEligibility(acquiredTradeEligibilityForm.fill(data)))
    //      case None => Ok(AcquiredTradeEligibility(acquiredTradeEligibilityForm))
    //    }
    Future.successful(Ok(AcquiredTradeEligibility(acquiredTradeEligibilityForm)))
  }

  val submit: Action[AnyContent] =  { Action.async { implicit request =>

    def routeRequest(acquiredTradeEligibilityModel: AcquiredTradeEligibilityModel): Result = {
      /*TODO get answers from keystore, if any not yes return to isAgent*/
      //      for {
      //        isGroupOrSub <- keystoreConnector.fetchAndGetFormData[GroupsAndSubsEligibilityModel](KeystoreKeys.groupsAndSubsEligibility)
      //        (isGroupOrSub,acquiredTrade) <- (isGroupOrSub.getOrElse()
      //      }
            tokenService.generateTemporaryToken match {
              case Ok => Redirect(controllers.routes.ApplicationHubController.show())
              case _ => InternalServerError(internalServerErrorTemplate)
            }
    }

    acquiredTradeEligibilityForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(AcquiredTradeEligibility(formWithErrors)))
      },
      validFormData => {
        keystoreConnector.saveFormData(KeystoreKeys.acquiredTradeEligibility, validFormData)
        validFormData.acquiredTrade match {
          case Constants.StandardRadioButtonYesValue => Future.successful(Redirect(""))
          case Constants.StandardRadioButtonNoValue => Future.successful(routeRequest(validFormData))
        }
      }
    )
  }
  }



}
