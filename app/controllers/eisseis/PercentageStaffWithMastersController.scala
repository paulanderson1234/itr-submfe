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

package controllers.eisseis

import auth.{AuthorisedAndEnrolledForTAVC,SEIS, EIS, VCT}
import common.{Constants, KeystoreKeys}
import config.FrontendGlobal._
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.predicates.FeatureSwitch
import forms.PercentageStaffWithMastersForm._
import models.{KiProcessingModel, PercentageStaffWithMastersModel}
import play.Logger
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import views.html.eisseis.knowledgeIntensive._

object PercentageStaffWithMastersController extends PercentageStaffWithMastersController{
  override lazy val s4lConnector = S4LConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait PercentageStaffWithMastersController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(EIS,SEIS,VCT),Seq(SEIS,VCT), Seq(EIS,SEIS))

  val submissionConnector: SubmissionConnector

  val show = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters).map {
        case Some(data) => Ok(PercentageStaffWithMasters(percentageStaffWithMastersForm.fill(data)))
        case None => Ok(PercentageStaffWithMasters(percentageStaffWithMastersForm))
      }
    }
  }

  val submit = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

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
            s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, updatedModel)

            if (updatedModel.isKi) {
              // it's all good - no need to ask more KI questions
              s4lConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries,
                routes.PercentageStaffWithMastersController.show().url)
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
          s4lConnector.saveFormData(KeystoreKeys.percentageStaffWithMasters, validFormData)
          val percentageWithMasters: Boolean = if (validFormData.staffWithMasters ==
            Constants.StandardRadioButtonYesValue) true
          else false
          (for {
            kiModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
            // Call API
            isSecondaryKiConditionsMet <- submissionConnector.validateSecondaryKiConditions(percentageWithMasters,
              if (kiModel.isDefined) kiModel.get.hasTenYearPlan.getOrElse(false) else false)
            route <- routeRequest(kiModel, percentageWithMasters, isSecondaryKiConditionsMet)
          } yield route) recover {
            case e: Exception => {
              Logger.warn(s"[PercentageStaffWithMastersController][submit] - Exception validateSecondaryKiConditions: ${e.getMessage}")
              InternalServerError(internalServerErrorTemplate)
            }
          }
        }
      )
    }
  }

  def isMissingData(data: KiProcessingModel): Boolean = {
    data.dateConditionMet.isEmpty || data.companyAssertsIsKi.isEmpty || data.costsConditionMet.isEmpty
  }
}
