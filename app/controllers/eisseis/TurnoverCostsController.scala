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
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import forms.TurnoverCostsForm._
import common._
import config.FrontendGlobal._
import models._
import models.submission.CostModel
import play.Logger
import play.api.libs.json.Json
import views.html.eisseis.investment.TurnoverCosts
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object TurnoverCostsController extends TurnoverCostsController {
  override lazy val s4lConnector = S4LConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait TurnoverCostsController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(EIS,SEIS,VCT),Seq(SEIS,VCT), Seq(EIS,SEIS))

  implicit val formatCostModel = Json.format[CostModel]

  val submissionConnector: SubmissionConnector

  val show = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](KeystoreKeys.turnoverCosts).map {
        case Some(data) => Ok(TurnoverCosts(turnoverCostsForm.fill(data)))
        case None => Ok(TurnoverCosts(turnoverCostsForm))
      }
    }
  }

  val submit = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      def routeRequest(subsidiaries: Option[SubsidiariesModel], turnoverCheckRes: Option[Boolean]): Future[Result] = {
        turnoverCheckRes match {
          case Some(true) => subsidiaries match {
            case Some(data) if data.ownSubsidiaries == Constants.StandardRadioButtonYesValue =>
              s4lConnector.saveFormData(KeystoreKeys.backLinkSubSpendingInvestment, routes.TurnoverCostsController.show().url)
              Future.successful(Redirect(routes.SubsidiariesSpendingInvestmentController.show()))
            case Some(_) =>
              s4lConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow, routes.TurnoverCostsController.show().url)
              Future.successful(Redirect(routes.InvestmentGrowController.show()))
            case _ => Future.successful(Redirect(routes.SubsidiariesController.show()))
          }
          case _ => Future.successful(Redirect(routes.AnnualTurnoverErrorController.show()))
        }
      }

      turnoverCostsForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(TurnoverCosts(formWithErrors)))
        },
        validFormData => {
          //TODO: add the annual aveage turnover check and navigtion to error or correct page etc..subsidiaries temporary
          s4lConnector.saveFormData(KeystoreKeys.turnoverCosts, validFormData)
          (for {
            subsidiaries <- s4lConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
            proposedInvestment <- s4lConnector.fetchAndGetFormData[ProposedInvestmentModel](KeystoreKeys.proposedInvestment)
            turnoverCheckRes <- submissionConnector.checkAveragedAnnualTurnover(proposedInvestment.get, validFormData)
            route <- routeRequest(subsidiaries, turnoverCheckRes)
          } yield route) recover {
            case e: NoSuchElementException => Redirect(routes.ProposedInvestmentController.show())
            case e: Exception => {
              Logger.warn(s"[PercentageStaffWithMastersController][submit] - Exception validateSecondaryKiConditions: ${e.getMessage}")
              InternalServerError(internalServerErrorTemplate)
            }
          }
        }
      )
    }
  }

}
