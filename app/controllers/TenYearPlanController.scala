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

import connectors.KeystoreConnector
import controllers.predicates.ValidActiveSession
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import models.TenYearPlanModel
import common._
import forms.TenYearPlanForm._
import utils.Validation
import views.html.knowledgeIntensive.TenYearPlan
import scala.concurrent.Future

object TenYearPlanController extends TenYearPlanController {
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait TenYearPlanController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan).map {
      case Some(data) => Ok(TenYearPlan(tenYearPlanForm.fill(data)))
      case None => Ok(TenYearPlan(tenYearPlanForm))
    }
  }

  val submit = Action.async { implicit request =>

    def routeRequest(description: Option[TenYearPlanModel]): Future[Result] = {
      description match {
        case Some(data) if Validation.anyFieldsEmpty(data.hasTenYearPlan, data.tenYearPlanDesc) =>
          Future.successful(Redirect(routes.SubsidiariesController.show()))
        case Some(_) => Future.successful(Redirect(routes.SubsidiariesController.show()))
        case None => Future.successful(Redirect(routes.TenYearPlanController.show()))
      }
    }

    tenYearPlanForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(TenYearPlan(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.tenYearPlan, validFormData)

        for {
          date <- keyStoreConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan)
          route <- routeRequest(date)
        } yield route
      }
    )
  }
}
