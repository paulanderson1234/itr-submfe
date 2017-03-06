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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.predicates.FeatureSwitch
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import models.{KiProcessingModel, TenYearPlanModel}
import common._
import config.FrontendGlobal._
import forms.TenYearPlanForm._
import views.html.eisseis.knowledgeIntensive.TenYearPlan
import play.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object TenYearPlanController extends TenYearPlanController {
  override lazy val s4lConnector = S4LConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait TenYearPlanController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(EIS,SEIS,VCT),Seq(SEIS,VCT), Seq(EIS,SEIS))


  val submissionConnector: SubmissionConnector

  val show = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan).map {
        case Some(data) => Ok(TenYearPlan(tenYearPlanForm.fill(data)))
        case None => Ok(TenYearPlan(tenYearPlanForm))
      }
    }
  }

  val submit = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      def routeRequest(kiModel: Option[KiProcessingModel], hasTenYearPlan: Boolean,
                       isSecondaryKiConditionsMet: Option[Boolean]): Future[Result] = {
        kiModel match {
          // check previous answers present
          case Some(data) if isMissingData(data) =>

            /** not sure if we are still using isMissingData **/
            Future.successful(Redirect(routes.DateOfIncorporationController.show()))
          case Some(dataWithPrevious) if !dataWithPrevious.companyAssertsIsKi.get =>
            Future.successful(Redirect(routes.IsKnowledgeIntensiveController.show()))
          case Some(dataWithPreviousValid) => {
            // all good - save the cost condition result returned from API and navigate accordingly
            val updatedModel = dataWithPreviousValid.copy(secondaryCondtionsMet = isSecondaryKiConditionsMet,
              hasTenYearPlan = Some(hasTenYearPlan))
            s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, updatedModel)

            // check the current model to see if valid as this is last page but user could navigate via url out of sequence
            if (updatedModel.isKi) {
              s4lConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries, routes.TenYearPlanController.show().url)
              Future.successful(Redirect(routes.SubsidiariesController.show()))
            }
            else {
              // KI condition not met. end of the road..
              s4lConnector.saveFormData(KeystoreKeys.backLinkIneligibleForKI, routes.TenYearPlanController.show().url)
              Future.successful(Redirect(routes.IneligibleForKIController.show()))
            }
          }
          case None => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
        }
      }

      tenYearPlanForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(TenYearPlan(formWithErrors)))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.tenYearPlan, validFormData)
          val hasTenYearPlan: Boolean = if (validFormData.hasTenYearPlan ==
            Constants.StandardRadioButtonYesValue) true
          else false
          (for {
            kiModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
            // Call API
            isSecondaryKiConditionsMet <- submissionConnector.validateSecondaryKiConditions(
              if (kiModel.isDefined) kiModel.get.hasPercentageWithMasters.getOrElse(false) else false, hasTenYearPlan)
            route <- routeRequest(kiModel, hasTenYearPlan, isSecondaryKiConditionsMet)
          } yield route) recover {
            case e: Exception => {
              Logger.warn(s"[TenYearPlanController][submit] - Exception validateSecondaryKiConditions: ${e.getMessage}")
              InternalServerError(internalServerErrorTemplate)
            }
          }
        }
      )
    }
  }

  def isMissingData(data: KiProcessingModel): Boolean = {
    data.dateConditionMet.isEmpty || data.companyAssertsIsKi.isEmpty ||
      data.costsConditionMet.isEmpty || data.hasPercentageWithMasters.isEmpty
  }

}
