/*
 * Copyright 2016 HM Revenue & Customs
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

package controllers

import common.KeystoreKeys
import connectors.KeystoreConnector
import forms.PreviousBeforeDOFCSForm._
import controllers.predicates.ValidActiveSession
import models.{SubsidiariesModel, PreviousBeforeDOFCSModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import views.html.investment.PreviousBeforeDOFCS
import scala.concurrent.Future
import views.html._

object PreviousBeforeDOFCSController extends PreviousBeforeDOFCSController {
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait PreviousBeforeDOFCSController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS).map {
      case Some(data) => Ok(investment.PreviousBeforeDOFCS(previousBeforeDOFCSForm.fill(data)))
      case None => Ok(investment.PreviousBeforeDOFCS(previousBeforeDOFCSForm))
    }
  }

  val submit = Action.async { implicit request =>

  def routeRequest(date: Option[SubsidiariesModel]): Future[Result] = {
    date match {
      case Some(data) if data.ownSubsidiaries == "Yes" =>
        Future.successful(Redirect(routes.PreviousBeforeDOFCSController.show))
      case Some(_) => Future.successful(Redirect(routes.PreviousBeforeDOFCSController.show))
      case None => Future.successful(Redirect(routes.SubsidiariesController.show))
    }
  }

  previousBeforeDOFCSForm.bindFromRequest().fold(
    formWithErrors => {
      Future.successful(BadRequest(PreviousBeforeDOFCS(formWithErrors)))
    },
    validFormData => {
      keyStoreConnector.saveFormData(KeystoreKeys.previousBeforeDOFCS, validFormData)
      validFormData.previousBeforeDOFCS match {
        case "No" => Future.successful(Redirect(routes.PreviousBeforeDOFCSController.show))
        case "Yes" => for {
          subsidiaries <- keyStoreConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
          route <- routeRequest(subsidiaries)
        } yield route
      }
    }
  )
}
}
