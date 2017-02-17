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

package controllers.eis

import auth.{AuthorisedAndEnrolledForTAVC, EIS, VCT}
import common.KeystoreKeys
import config.FrontendGlobal._
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import forms.OperatingCostsForm._
import models.{KiProcessingModel, OperatingCostsModel}
import play.Logger
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.knowledgeIntensive.OperatingCosts
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future


object OperatingCostsController extends OperatingCostsController{
  override lazy val s4lConnector = S4LConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait OperatingCostsController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))


  val submissionConnector: SubmissionConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts).map {
      case Some(data) => Ok(OperatingCosts(operatingCostsForm.fill(data)))
      case None => Ok(OperatingCosts(operatingCostsForm))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>

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
          s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, dataWithDateConditionMet.copy(costsConditionMet = isCostConditionMet))

          isCostConditionMet match {
            case Some(data) =>

              if(data){
                Future.successful(Redirect(routes.PercentageStaffWithMastersController.show()))
              } else {
                s4lConnector.saveFormData(KeystoreKeys.backLinkIneligibleForKI, routes.OperatingCostsController.show().toString())
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
        s4lConnector.saveFormData(KeystoreKeys.operatingCosts, validFormData)

        (for {
          kiModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
          // Call API
          costConditionMet <- submissionConnector.validateKiCostConditions(
            validFormData.operatingCosts1stYear.toInt,
            validFormData.operatingCosts2ndYear.toInt,
            validFormData.operatingCosts3rdYear.toInt,
            validFormData.rAndDCosts1stYear.toInt,
            validFormData.rAndDCosts2ndYear.toInt,
            validFormData.rAndDCosts3rdYear.toInt
          )
          route <- routeRequest(kiModel, costConditionMet)
        } yield route) recover{
          case e: Exception => {
            Logger.warn(s"[OperatingCostsController][submit] - Exception checking Ki Conditions: ${e.getMessage}")
            InternalServerError(internalServerErrorTemplate)
          }
        }
      }
    )
  }

  def isMissingData(data: KiProcessingModel): Boolean = {
    data.dateConditionMet.isEmpty || data.companyAssertsIsKi.isEmpty
  }

}
