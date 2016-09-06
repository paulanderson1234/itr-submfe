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

import common.{Constants, KeystoreKeys}
import connectors.{KeystoreConnector, SubmissionConnector}
import controllers.predicates.ValidActiveSession
import forms.OperatingCostsForm._
import forms.PercentageStaffWithMastersForm._
import models.{KiProcessingModel, PercentageStaffWithMastersModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._

import scala.concurrent.Future
import views.html._
import views.html.knowledgeIntensive.{OperatingCosts, PercentageStaffWithMasters}

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

    def routeRequest(kiModel: Option[KiProcessingModel], percentageWithMasters: Boolean,
                     isSecondaryKiConditionsMet: Option[Boolean]) = {
      kiModel match {
        // check previous answers present
        case Some(data) if isMissingData(data) => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
        case Some(dataWithPrevious) if !dataWithPrevious.companyAssertsIsKi.get => {
          Future.successful(Redirect(routes.IsKnowledgeIntensiveController.show()))
        }
        case Some(dataWithPreviousValid) => {
          // all good - save the cost condition result returned from API and navigate accordingly
          val updatedModel = dataWithPreviousValid.copy(secondaryCondtionsMet = isSecondaryKiConditionsMet,
            hasPercentageWithMasters = Some(percentageWithMasters))
          keyStoreConnector.saveFormData(KeystoreKeys.kiProcessingModel, updatedModel)

          if (updatedModel.isKi) {
            // it's all good - no need to ask more KI questions
            keyStoreConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries,
              routes.PercentageStaffWithMastersController.show().toString())
            Future.successful(Redirect(routes.SubsidiariesController.show()))
          }
          else {
            // Non cost KI condition not met. Try other conditions.
            Future.successful(Redirect(routes.TenYearPlanController.show()))
          }
        }
        case None => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
      }
    }

    percentageStaffWithMastersForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(PercentageStaffWithMasters(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.percentageStaffWithMasters, validFormData)
        val percentageWithMasters: Boolean = if (validFormData.staffWithMasters ==
          Constants.StandardRadioButtonYesValue) true
        else false
        for {
          kiModel <- keyStoreConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
          // Call API
          isSecondaryKiConditionsMet <- SubmissionConnector.validateSecondaryKiConditions(percentageWithMasters,
            if (kiModel.isDefined) kiModel.get.hasTenYearPlan.getOrElse(false) else false) //TO DO - PROPER API CALL
          route <- routeRequest(kiModel, percentageWithMasters, isSecondaryKiConditionsMet)
        } yield route
      }
    )
  }

  def isMissingData(data: KiProcessingModel): Boolean = {
    data.dateConditionMet.isEmpty || data.companyAssertsIsKi.isEmpty || data.costsConditionMet.isEmpty
  }
}
