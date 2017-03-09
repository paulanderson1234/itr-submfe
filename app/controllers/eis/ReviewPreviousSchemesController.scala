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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{ControllerHelpers, PreviousSchemesHelper}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.previousInvestment.ReviewPreviousSchemes
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object ReviewPreviousSchemesController extends ReviewPreviousSchemesController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ReviewPreviousSchemesController extends FrontendController with AuthorisedAndEnrolledForTAVC with PreviousSchemesHelper {

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    def routeRequest(backUrl: Option[String]) = {
      if (backUrl.isDefined) {
        PreviousSchemesHelper.getAllInvestmentFromKeystore(s4lConnector).flatMap{
          previousSchemes =>
            if(previousSchemes.nonEmpty) {
              Future.successful(Ok(ReviewPreviousSchemes(previousSchemes,backUrl.get)))
            }
            else Future.successful(Redirect(routes.HadPreviousRFIController.show()))
        }
      } else {
        // no back link - send to beginning of flow
        Future.successful(Redirect(routes.HadPreviousRFIController.show()))
      }
    }
    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkReviewPreviousSchemes, s4lConnector)
      route <- routeRequest(link)
    } yield route
  }

  def add: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.saveFormData(KeystoreKeys.backLinkPreviousScheme, routes.ReviewPreviousSchemesController.show().url)
    Future.successful(Redirect(routes.PreviousSchemeController.show(None)))
  }

  def change(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.saveFormData(KeystoreKeys.backLinkPreviousScheme, routes.ReviewPreviousSchemesController.show().url)
    Future.successful(Redirect(routes.PreviousSchemeController.show(Some(id))))
  }

  def remove(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
      Future.successful(Redirect(routes.DeletePreviousSchemeController.show(id)))
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.saveFormData(KeystoreKeys.backLinkProposedInvestment, routes.ReviewPreviousSchemesController.show().url)
    PreviousSchemesHelper.getAllInvestmentFromKeystore(s4lConnector).flatMap(previousSchemes =>
      if(previousSchemes.nonEmpty) Future.successful(Redirect(routes.ProposedInvestmentController.show()))
      else Future.successful(Redirect(routes.ReviewPreviousSchemesController.show())))
  }
}
