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
import controllers.predicates.ValidActiveSession
import forms.PercentageStaffWithMastersForm._
import models.PercentageStaffWithMastersModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import scala.concurrent.Future
import views.html._

object PercentageStaffWithMastersController extends PercentageStaffWithMastersController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait PercentageStaffWithMastersController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters).map {
      case Some(data) => Ok(knowledgeIntensive.PercentageStaffWithMasters(percentageStaffWithMastersForm.fill(data)))
      case None => Ok(knowledgeIntensive.PercentageStaffWithMasters(percentageStaffWithMastersForm))
    }
  }

  val submit = Action.async { implicit request =>
    val response = percentageStaffWithMastersForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(knowledgeIntensive.PercentageStaffWithMasters(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.percentageStaffWithMasters, validFormData)
        validFormData.staffWithMasters match {
          case "Yes"  => Redirect(routes.SubsidiariesController.show)
          case "No"   => Redirect(routes.TenYearPlanController.show)
        }
      }
    )
    Future.successful(response)
  }
}
