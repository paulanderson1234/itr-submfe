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

import auth.{AuthorisedAndEnrolledForTAVC, EIS, TAVCUser, VCT}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.Helpers.{ControllerHelpers, PreviousSchemesHelper}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import models.{CommercialSaleModel, HadPreviousRFIModel, KiProcessingModel, ProposedInvestmentModel, SubsidiariesModel}
import common.{Constants, KeystoreKeys}
import config.FrontendGlobal._
import forms.ProposedInvestmentForm._
import play.Logger
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.Validation
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import views.html.eis.investment.ProposedInvestment

object ProposedInvestmentController extends ProposedInvestmentController
{
  override lazy val s4lConnector = S4LConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ProposedInvestmentController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))

  val submissionConnector: SubmissionConnector

  val show: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
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

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeReq(kiModel: KiProcessingModel, prevRFI: HadPreviousRFIModel,
                 comSale: Option[CommercialSaleModel], hasSub: Option[SubsidiariesModel]): Future[Result] = {
      getRoute(prevRFI, comSale, hasSub, kiModel.isKi)
    }

    def routeRequest(kiModel: Option[KiProcessingModel], isLifeTimeAllowanceExceeded: Option[Boolean], prevRFI: HadPreviousRFIModel): Future[Result] = {
      kiModel match {
        // check previous answers present
        case Some(dataWithPreviousValid) => {
          // all good - TODO:Save the lifetime exceeded flag? - decide how to handle. For now I put it in keystore..
          if(isLifeTimeAllowanceExceeded.nonEmpty){
            s4lConnector.saveFormData(KeystoreKeys.lifeTimeAllowanceExceeded, isLifeTimeAllowanceExceeded.getOrElse(false))
          }

          isLifeTimeAllowanceExceeded match {
            case Some(data) =>
              // if it's exceeded go to the error page
              if (data) {
                Future.successful(Redirect(routes.LifetimeAllowanceExceededController.show()))
              }
              else {
                // not exceeded - continue
                for {
                  comSale <- s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
                  hasSub <- s4lConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
                  route <- routeReq(kiModel.get, prevRFI, comSale, hasSub)
                } yield route
              }

            // if none, redirect back to HadPreviousRFI page.
            // Will only hit this if there is no backend connected.
            case None => Future.successful(Redirect(routes.HadPreviousRFIController.show()))
          }
        }
        case None => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
      }
    }

    proposedInvestmentForm.bindFromRequest().fold(
      formWithErrors => {
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkProposedInvestment, s4lConnector).flatMap(url =>
          Future.successful(BadRequest(ProposedInvestment(formWithErrors,
            url.getOrElse(routes.HadPreviousRFIController.show().toString)))))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.proposedInvestment, validFormData)
        (for {
          kiModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
          hadPrevRFI <- s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
          previousInvestments <- PreviousSchemesHelper.getPreviousInvestmentTotalFromKeystore(s4lConnector)

          // Call API
          isLifeTimeAllowanceExceeded <- submissionConnector.checkLifetimeAllowanceExceeded(
            if (hadPrevRFI.get.hadPreviousRFI == Constants.StandardRadioButtonYesValue) true else false,
            if (kiModel.isDefined) kiModel.get.isKi else false, previousInvestments,
            validFormData.investmentAmount)

          route <- routeRequest(kiModel, isLifeTimeAllowanceExceeded, hadPrevRFI.get)
        } yield route) recover {
          case e: NoSuchElementException => Redirect(routes.HadPreviousRFIController.show())
          case e: Exception => {
            Logger.warn(s"[PercentageStaffWithMastersController][submit] - Exception validateSecondaryKiConditions: ${e.getMessage}")
            InternalServerError(internalServerErrorTemplate)
          }
        }
      }
    )
  }
  def getRoute(prevRFI: HadPreviousRFIModel, commercialSale: Option[CommercialSaleModel], hasSub: Option[SubsidiariesModel], isKi: Boolean)
              (implicit hc: HeaderCarrier, user: TAVCUser): Future[Result] = {

    commercialSale match {
      case Some(sale) if sale.hasCommercialSale == Constants.StandardRadioButtonNoValue => subsidiariesCheck(hasSub)
      case Some(sale) if sale.hasCommercialSale == Constants.StandardRadioButtonYesValue => getPreviousSaleRoute(prevRFI, sale, hasSub, isKi)
      case None => Future.successful(Redirect(routes.CommercialSaleController.show()))
    }
  }

  def getAgeLimit(isKI: Boolean): Int = {
    if (isKI) Constants.IsKnowledgeIntensiveYears
    else Constants.IsNotKnowledgeIntensiveYears
  }

  def subsidiariesCheck(hasSub: Option[SubsidiariesModel])(implicit hc: HeaderCarrier, user: TAVCUser): Future[Result] = {
    hasSub match {
      case Some(data) => if (data.ownSubsidiaries.equals(Constants.StandardRadioButtonYesValue)) {
        s4lConnector.saveFormData(KeystoreKeys.backLinkSubSpendingInvestment,
          routes.ProposedInvestmentController.show().url)
        Future.successful(Redirect(routes.SubsidiariesSpendingInvestmentController.show()))
      } else {
        s4lConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow,
          routes.ProposedInvestmentController.show().url)
        Future.successful(Redirect(routes.InvestmentGrowController.show()))
      }
      case None => {
        s4lConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries,
          routes.ProposedInvestmentController.show().url)
        Future.successful(Redirect(routes.SubsidiariesController.show()))
      }
    }
  }

  def getPreviousSaleRoute(prevRFI: HadPreviousRFIModel, commercialSale: CommercialSaleModel, hasSub: Option[SubsidiariesModel], isKi: Boolean)
                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[Result] = {

    val dateWithinRangeRule: Boolean = Validation.checkAgeRule(commercialSale.commercialSaleDay.get,
      commercialSale.commercialSaleMonth.get, commercialSale.commercialSaleYear.get, getAgeLimit(isKi))

    prevRFI match {
      case rfi if rfi.hadPreviousRFI == Constants.StandardRadioButtonNoValue => {
        // this is first scheme
        if (dateWithinRangeRule) {
          s4lConnector.saveFormData(KeystoreKeys.backLinkNewGeoMarket,
            routes.ProposedInvestmentController.show().url)
          Future.successful(Redirect(routes.NewGeographicalMarketController.show()))
        }
        else subsidiariesCheck(hasSub)
      }
      case rfi if rfi.hadPreviousRFI == Constants.StandardRadioButtonYesValue => {
        // subsequent scheme
        if (dateWithinRangeRule) Future.successful(Redirect(routes.UsedInvestmentReasonBeforeController.show()))
        else subsidiariesCheck(hasSub)
      }

    }
  }
}
