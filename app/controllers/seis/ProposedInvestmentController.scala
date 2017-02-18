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

package controllers.seis

import auth.{AuthorisedAndEnrolledForTAVC, SEIS}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.Helpers.ControllerHelpers
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import controllers.predicates.FeatureSwitch
import forms.ProposedInvestmentForm._
import models.ProposedInvestmentModel
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import views.html.seis.investment.ProposedInvestment

object ProposedInvestmentController extends ProposedInvestmentController {
  override lazy val s4lConnector = S4LConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ProposedInvestmentController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))


  val submissionConnector: SubmissionConnector

  def show: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      def routeRequest(backUrl: Option[String]) = {
        if (backUrl.isDefined) {
          s4lConnector.fetchAndGetFormData[ProposedInvestmentModel](KeystoreKeys.proposedInvestment).map {
            case Some(data) => Ok(ProposedInvestment(proposedInvestmentForm.fill(data), backUrl.get))
            case None => Ok(ProposedInvestment(proposedInvestmentForm, backUrl.get))
          }
        } else {
          // no back link - send to beginning of flow
          Future.successful(Redirect(routes.HadPreviousRFIController.show()))
        }
      }
      for {
        link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkProposedInvestment, s4lConnector)
        route <- routeRequest(link)
      } yield route
    }
  }

  def submit: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async { implicit user => implicit request =>
      proposedInvestmentForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkProposedInvestment, s4lConnector).map {
            url => BadRequest(ProposedInvestment(formWithErrors,
                url.getOrElse(routes.HadPreviousRFIController.show().url)))
          }
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.proposedInvestment, validFormData)
          Future.successful(Redirect(routes.ConfirmContactDetailsController.show()))
        }
      )
    }
  }

}
