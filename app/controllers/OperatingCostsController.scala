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
import controllers.Helpers.KnowledgeIntensiveHelper
import controllers.predicates.ValidActiveSession
import forms.IsKnowledgeIntensiveForm._
import forms.OperatingCostsForm._
import models.{KiProcessingModel, OperatingCostsModel}
import play.api.mvc.{Action, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.companyDetails.IsKnowledgeIntensive
import views.html.knowledgeIntensive.OperatingCosts

import scala.concurrent.Future


object OperatingCostsController extends OperatingCostsController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
}

trait OperatingCostsController extends FrontendController with ValidActiveSession {
  val keyStoreConnector: KeystoreConnector
  val submissionConnector: SubmissionConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts).map {
      case Some(data) => Ok(OperatingCosts(operatingCostsForm.fill(data)))
      case None => Ok(OperatingCosts(operatingCostsForm))
    }
  }

  val submit = Action.async { implicit request =>

    def routeRequest(kiModel: Option[KiProcessingModel], isCostConditionMet: Option[Boolean]): Future[Result] = {
      kiModel match {
        case Some(data) if isMissingData(data) => {
          // missing expected data - send user back
          Future.successful(Redirect(routes.DateOfIncorporationController.show()))
        }
        case Some(dataWithPrevious) if !dataWithPrevious.companyAssertsIsKi.get => {
          Future.successful(Redirect(routes.IsKnowledgeIntensiveController.show()))
        }
        case Some(dataWithDateConditionMet) => {
          // all good - save the cost condition returned from API and navigate accordingly
          keyStoreConnector.saveFormData(KeystoreKeys.kiProcessingModel, dataWithDateConditionMet.copy(costsConditionMet = isCostConditionMet))

          isCostConditionMet match {
            case Some(data) =>

              if(data){
                Future.successful(Redirect(routes.PercentageStaffWithMastersController.show()))
              } else {
                keyStoreConnector.saveFormData(KeystoreKeys.backLinkIneligibleForKI, routes.OperatingCostsController.show().toString())
                Future.successful(Redirect(routes.IneligibleForKIController.show()))
              }
              //will only hit case none if the back end isn't running.
            case None => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
          }
        }
        case None => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
      }
    }

    operatingCostsForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(OperatingCosts(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.operatingCosts, validFormData)

        for {
          kiModel <- keyStoreConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
          // Call API
          costConditionMet <- submissionConnector.validateKiCostConditions(
            validFormData.operatingCosts1stYear.toInt,
            validFormData.operatingCosts2ndYear.toInt,
            validFormData.operatingCosts3rdYear.toInt,
            validFormData.rAndDCosts1stYear.toInt,
            validFormData.rAndDCosts2ndYear.toInt,
            validFormData.rAndDCosts3rdYear.toInt
          ) //TO DO - PROPER API CALL
          route <- routeRequest(kiModel, costConditionMet)
        } yield route
      }
    )
  }

  def isMissingData(data: KiProcessingModel): Boolean = {
    data.dateConditionMet.isEmpty || data.companyAssertsIsKi.isEmpty
  }

}
