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
import connectors.KeystoreConnector
import controllers.predicates.ValidActiveSession
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.throttlingGuidance.GroupsAndSubsEligibility

import scala.concurrent.Future
import forms.throttlingGuidance.GroupsAndSubsEligibilityForm._
import models.throttlingGuidance.GroupsAndSubsEligibilityModel
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

trait GroupsAndSubsEligibilityController extends FrontendController with ValidActiveSession{

  val keystoreConnector: KeystoreConnector

  val show: Action[AnyContent] = ValidateSession.async { implicit request =>
      keystoreConnector.fetchAndGetFormData[GroupsAndSubsEligibilityModel](KeystoreKeys.groupsAndSubsEligibility) map {
        case Some(data) => Ok(GroupsAndSubsEligibility(groupsAndSubsEligibilityForm.fill(data)))
        case None => Ok(GroupsAndSubsEligibility(groupsAndSubsEligibilityForm))
      }
  }

  val submit: Action[AnyContent] =  { ValidateSession.async { implicit request =>
    groupsAndSubsEligibilityForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(GroupsAndSubsEligibility(formWithErrors)))
      },
      validFormData => {
        keystoreConnector.saveFormData(KeystoreKeys.groupsAndSubsEligibility, validFormData)
        validFormData.isGroupOrSub match {
          case Constants.StandardRadioButtonYesValue => Future.successful(Redirect(controllers.throttlingGuidance.routes.IsGroupErrorController.show()))
          case Constants.StandardRadioButtonNoValue => Future.successful(Redirect(routes.AcquiredTradeEligibilityController.show()))
        }
      }
    )
  }
  }
}

object GroupsAndSubsEligibilityController extends GroupsAndSubsEligibilityController{
  override val keystoreConnector: KeystoreConnector = KeystoreConnector
}
