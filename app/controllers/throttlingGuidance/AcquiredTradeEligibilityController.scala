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
import config.FrontendGlobal._
import connectors.KeystoreConnector
import controllers.predicates.ValidActiveSession
import models.throttlingGuidance.{AcquiredTradeEligibilityModel, GroupsAndSubsEligibilityModel, IsAgentModel}
import play.api.mvc.{Action, AnyContent, Result}
import services.TokenService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.throttlingGuidance.AcquiredTradeEligibility

import scala.concurrent.Future
import forms.throttlingGuidance.AcquiredTradeEligibilityForm._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

trait AcquiredTradeEligibilityController extends FrontendController with ValidActiveSession {

  val keystoreConnector: KeystoreConnector
  val tokenService: TokenService

  val show: Action[AnyContent] = ValidateSession.async { implicit request =>
    def routeRequest: Future[Result] = {
      keystoreConnector.fetchAndGetFormData[AcquiredTradeEligibilityModel](KeystoreKeys.acquiredTradeEligibility) map {
        case Some(data) => Ok(AcquiredTradeEligibility(acquiredTradeEligibilityForm.fill(data)))
        case None => Ok(AcquiredTradeEligibility(acquiredTradeEligibilityForm))
      }
    }

    keystoreConnector.fetchAndGetFormData[Boolean](KeystoreKeys.throttleCheckPassed) flatMap {
      throttleCheckPassed => if (throttleCheckPassed.getOrElse(false)) routeRequest
      else Future.successful(Redirect(controllers.throttlingGuidance.routes.OurServiceChangeController.show()))
    }
  }

  val submit: Action[AnyContent] = {
    ValidateSession.async { implicit request =>

      def routeRequest: Future[Result] = {
        def routeReq(isAgentModel: Option[IsAgentModel], isGroupOrSubModel: Option[GroupsAndSubsEligibilityModel]): Future[Result] = {
          if (isAgentModel.isDefined && isAgentModel.get.isAgent == Constants.StandardRadioButtonNoValue &&
            (isGroupOrSubModel.isDefined && isGroupOrSubModel.get.isGroupOrSub == Constants.StandardRadioButtonNoValue)) tokenService.generateTemporaryToken map {
            tokenId => {
              if (tokenId.nonEmpty) Redirect(controllers.routes.ApplicationHubController.show(Some(tokenId)))
              else InternalServerError(internalServerErrorTemplate)
            }
          } else {
            Future.successful(Redirect(controllers.throttlingGuidance.routes.IsAgentController.show()))
          }
        }

        for {
          isAgent <- keystoreConnector.fetchAndGetFormData[IsAgentModel](KeystoreKeys.isAgentEligibility)
          isGroupOrSub <- keystoreConnector.fetchAndGetFormData[GroupsAndSubsEligibilityModel](KeystoreKeys.groupsAndSubsEligibility)
          route <- routeReq(isAgent, isGroupOrSub)
        } yield route
      }

      acquiredTradeEligibilityForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(AcquiredTradeEligibility(formWithErrors)))
        },
        validFormData => {
          keystoreConnector.saveFormData(KeystoreKeys.acquiredTradeEligibility, validFormData)
          validFormData.acquiredTrade match {
            case Constants.StandardRadioButtonYesValue => Future.successful(Redirect(controllers.throttlingGuidance.routes.
              IsAcquiredTradeErrorController.show()))
            case Constants.StandardRadioButtonNoValue => routeRequest
          }
        }
      )
    }
  }

}

object AcquiredTradeEligibilityController extends AcquiredTradeEligibilityController{
  override val keystoreConnector: KeystoreConnector = KeystoreConnector
  override val tokenService: TokenService = TokenService
}
