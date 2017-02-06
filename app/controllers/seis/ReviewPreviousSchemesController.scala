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

import auth.AuthorisedAndEnrolledForTAVC
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.Helpers.{ControllerHelpers, PreviousSchemesHelper}
import controllers.featureSwitch.SEISFeatureSwitch
import controllers.routes
import forms.PreviousSchemeForm._
import models.{DateOfIncorporationModel, HadPreviousRFIModel}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.previousInvestment.{PreviousScheme, ReviewPreviousSchemes}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc._

import scala.concurrent.Future

object ReviewPreviousSchemesController extends ReviewPreviousSchemesController {
  val s4lConnector: S4LConnector = S4LConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ReviewPreviousSchemesController extends FrontendController with AuthorisedAndEnrolledForTAVC with PreviousSchemesHelper with SEISFeatureSwitch {

  val s4lConnector: S4LConnector
  val submissionConnector: SubmissionConnector

  val show = seisFeatureSwitch {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      def routeRequest(backUrl: Option[String]) = {
        if (backUrl.isDefined) {
          PreviousSchemesHelper.getAllInvestmentFromKeystore(s4lConnector).flatMap {
            previousSchemes =>
              if (previousSchemes.nonEmpty) {
                Future.successful(Ok(ReviewPreviousSchemes(previousSchemes, backUrl.get)))
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
  }

  def add: Action[AnyContent] = seisFeatureSwitch {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.saveFormData(KeystoreKeys.backLinkPreviousScheme, routes.ReviewPreviousSchemesController.show().url)
      Future.successful(Redirect(routes.PreviousSchemeController.show(None)))
    }
  }

  def change(id: Int): Action[AnyContent] = seisFeatureSwitch {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.saveFormData(KeystoreKeys.backLinkPreviousScheme, routes.ReviewPreviousSchemesController.show().url)
      Future.successful(Redirect(routes.PreviousSchemeController.show(Some(id))))
    }
  }

  def remove(id: Int): Action[AnyContent] = seisFeatureSwitch {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.saveFormData(KeystoreKeys.backLinkPreviousScheme, routes.ReviewPreviousSchemesController.show().url)
      PreviousSchemesHelper.removeKeystorePreviousInvestment(s4lConnector, id).map {
        _ => Redirect(routes.ReviewPreviousSchemesController.show())
      }
    }
  }

  val submit = seisFeatureSwitch {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      def routeRequest(isLifeTimeAllowanceExceeded: Option[Boolean]): Future[Result] = {

        Future.successful(Redirect(routes.ReviewPreviousSchemesController.show()))
      }


      s4lConnector.saveFormData(KeystoreKeys.backLinkProposedInvestment, routes.ReviewPreviousSchemesController.show().url)


      (for{
        investmentsSinceStartDate <- PreviousSchemesHelper.getPreviousInvestmentsFromStartDateTotal(s4lConnector)
        hadPrevRFI <- s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)

        //CALL API
        //isLifeTimeAllowanceExceeded <- if (previousInvestmentsTotal > 0 submissionConnector.checkSeisPreviousInvestmentsAllowanceExceeded(22) else Future(false)
        isLifeTimeAllowanceExceeded <- submissionConnector.seisPreviousInvestmentAllowanceExceeded(investmentsSinceStartDate)


        route <- routeRequest(isLifeTimeAllowanceExceeded)
      //
      //      PreviousSchemesHelper.getAllInvestmentFromKeystore(s4lConnector).flatMap(previousSchemes =>
      //        if (previousSchemes.nonEmpty) {
      //          (for {
      //
      //            isLifeTimeAllowanceExceeded <- submissionConnector.checkLifetimeAllowanceExceeded(
      //              )
      //            route <- routeRequest(isLifeTimeAllowanceExceeded)
      //          } yield route) recover {
      //            case e : NoSuchElementException => Redirect(routes.HadPreviousRFIController.show())
      //          }


      //})
    } yield route) recover {
        case e: NoSuchElementException => Redirect(routes.ProposedInvestmentController.show())
      }
    }
  }

  val submit2 = seisFeatureSwitch {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.saveFormData(KeystoreKeys.backLinkProposedInvestment, routes.ReviewPreviousSchemesController.show().url)
      PreviousSchemesHelper.getAllInvestmentFromKeystore(s4lConnector).flatMap(previousSchemes =>
        if (previousSchemes.nonEmpty) Future.successful(Redirect(routes.ProposedInvestmentController.show()))
        else Future.successful(Redirect(routes.ReviewPreviousSchemesController.show())))
    }
  }
}
