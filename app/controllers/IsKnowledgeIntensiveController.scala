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

import auth.AuthorisedForTAVC
import common.{Constants, KeystoreKeys}
import config.{FrontendAuthConnector, FrontendAppConfig}
import connectors.KeystoreConnector
import forms.IsKnowledgeIntensiveForm._
import models._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._

import scala.concurrent.Future
import views.html._
import views.html.companyDetails.IsKnowledgeIntensive

object IsKnowledgeIntensiveController extends IsKnowledgeIntensiveController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
}

trait IsKnowledgeIntensiveController extends FrontendController with AuthorisedForTAVC {

  val keyStoreConnector: KeystoreConnector

  val show = Authorised.async { implicit  user => implicit request =>
    keyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](KeystoreKeys.isKnowledgeIntensive).map {
      case Some(data) => Ok(companyDetails.IsKnowledgeIntensive(isKnowledgeIntensiveForm.fill(data)))
      case None => Ok(companyDetails.IsKnowledgeIntensive(isKnowledgeIntensiveForm))
    }
  }

  val submit = Authorised.async { implicit user => implicit request =>

    def routeRequest(kiModel: Option[KiProcessingModel], isKnowledgeIntensive: Boolean): Future[Result] = {
      kiModel match {
        case Some(data) if data.dateConditionMet.isEmpty => {
          Future.successful(Redirect(routes.DateOfIncorporationController.show()))
        }
        case Some(dataWithDateCondition) => {
          if (!isKnowledgeIntensive & dataWithDateCondition.companyAssertsIsKi.getOrElse(false)) {
            // user changed from yes to no. Clear the processing data (keeping the date and isKi info)
            keyStoreConnector.saveFormData(KeystoreKeys.kiProcessingModel,
              KiProcessingModel(companyAssertsIsKi = Some(isKnowledgeIntensive),
                dateConditionMet = dataWithDateCondition.dateConditionMet))

            // clear real data: TODO: it will work for now but we should probably clear the real data to ..how do this??
            //keyStoreConnector.saveFormData(KeystoreKeys.operatingCosts, Option[OperatingCostsModel] = None)
            //keyStoreConnector.saveFormData(KeystoreKeys.tenYearPlan, Option[TenYearPlanModel] = None)
            //keyStoreConnector.saveFormData(KeystoreKeys.percentageStaffWithMasters, PercentageStaffWithMastersModel] = None)

            // go to subsidiaries
            keyStoreConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries, routes.IsKnowledgeIntensiveController.show().toString())
            Future.successful(Redirect(routes.SubsidiariesController.show()))

          }
          else {
            keyStoreConnector.saveFormData(KeystoreKeys.kiProcessingModel, dataWithDateCondition.copy(companyAssertsIsKi = Some(isKnowledgeIntensive)))
            if (isKnowledgeIntensive) Future.successful(Redirect(routes.OperatingCostsController.show()))
            else {
              keyStoreConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries, routes.IsKnowledgeIntensiveController.show().toString())
              Future.successful(Redirect(routes.SubsidiariesController.show()))
            }
          }

        }
        case None => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
      }
    }

    isKnowledgeIntensiveForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(IsKnowledgeIntensive(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.isKnowledgeIntensive, validFormData)
        for {
          kiModel <- keyStoreConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
          route <- routeRequest(kiModel, if (validFormData.isKnowledgeIntensive == Constants.StandardRadioButtonYesValue) true else false)
        } yield route
      }
    )
  }
}
