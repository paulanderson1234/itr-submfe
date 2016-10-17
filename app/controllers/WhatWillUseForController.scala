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

import auth.AuthorisedAndEnrolledForTAVC
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import forms.WhatWillUseForForm._
import models._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc.Result
import utils.Validation
import views.html.investment.WhatWillUseFor
import common.Constants
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object WhatWillUseForController extends WhatWillUseForController{
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  val s4lConnector: S4LConnector = S4LConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait WhatWillUseForController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val s4lConnector: S4LConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[WhatWillUseForModel](KeystoreKeys.whatWillUseFor).map {
      case Some(data) => Ok(WhatWillUseFor(whatWillUseForForm.fill(data)))
      case None => Ok(WhatWillUseFor(whatWillUseForForm))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(kiModel: Option[KiProcessingModel], prevRFI: Option[HadPreviousRFIModel],
                     comSale: Option[CommercialSaleModel], hasSub: Option[SubsidiariesModel]): Future[Result] = {
      kiModel match {
        case Some(data) if isMissingKiData(data) =>
          Future.successful(Redirect(routes.DateOfIncorporationController.show()))
        case Some(data) =>
          // ki data present and appears OK - get the route
          getRoute(hc, prevRFI, comSale, hasSub, data.isKi)
        case _ => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
      }
    }

    whatWillUseForForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(WhatWillUseFor(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.whatWillUseFor, validFormData)
        for {
          kiModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
          prevRFI <- s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
          comSale <- s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
          hasSub <- s4lConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
          route <- routeRequest(kiModel, prevRFI, comSale, hasSub)
        } yield route
      }
    )
  }

  def getRoute(implicit hc: HeaderCarrier, prevRFI: Option[HadPreviousRFIModel],
               commercialSale: Option[CommercialSaleModel],
               hasSub: Option[SubsidiariesModel], isKi: Boolean): Future[Result] = {

    commercialSale match {
      case Some(sale) if sale.hasCommercialSale == Constants.StandardRadioButtonNoValue => {
        subsidiariesCheck(hc, hasSub)
      }
      case Some(sale) if sale.hasCommercialSale == Constants.StandardRadioButtonYesValue => {
        getPreviousSaleRoute(hc, prevRFI, sale, hasSub, isKi)
      }
      case None => Future.successful(Redirect(routes.CommercialSaleController.show()))
    }
  }

  def isMissingKiData(data: KiProcessingModel): Boolean = {

    false

//    if(data.companyAssertsIsKi.isEmpty){
//      true
//    }
//    else if (data.companyAssertsIsKi.get){
//      if(data.costsConditionMet.isEmpty){
//        true
//      } else {
//        if (!data.costsConditionMet.get){
//          data.secondaryCondtionsMet.isEmpty
//        } else false
//      }
//    }
//    else if (data.dateConditionMet.isEmpty) {
//      true
//    }
//    else {
//      false
//    }
  }

  def getAgeLimit(isKI: Boolean): Int = {
    if (isKI) Constants.IsKnowledgeIntensiveYears
    else Constants.IsNotKnowledgeIntensiveYears
  }

  def subsidiariesCheck(implicit hc: HeaderCarrier, hasSub: Option[SubsidiariesModel]): Future[Result] = {
    hasSub match {
      case Some(data) => if (data.ownSubsidiaries.equals(Constants.StandardRadioButtonYesValue)) {
        s4lConnector.saveFormData(KeystoreKeys.backLinkSubSpendingInvestment,
          routes.WhatWillUseForController.show().toString())
        Future.successful(Redirect(routes.SubsidiariesSpendingInvestmentController.show()))
      } else {
        s4lConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow,
          routes.WhatWillUseForController.show().toString())
        Future.successful(Redirect(routes.InvestmentGrowController.show()))
      }
      case None => {
        s4lConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries,
          routes.WhatWillUseForController.show().toString())
        Future.successful(Redirect(routes.SubsidiariesController.show()))
      }
    }
  }

  def getPreviousSaleRoute(implicit hc: HeaderCarrier, prevRFI: Option[HadPreviousRFIModel],
                           commercialSale: CommercialSaleModel,
                           hasSub: Option[SubsidiariesModel], isKi: Boolean): Future[Result] = {

    val dateWithinRangeRule: Boolean = Validation.checkAgeRule(commercialSale.commercialSaleDay.get,
      commercialSale.commercialSaleMonth.get, commercialSale.commercialSaleYear.get, getAgeLimit(isKi))

    prevRFI match {
      case Some(rfi) if rfi.hadPreviousRFI == Constants.StandardRadioButtonNoValue => {
        // this is first scheme
        if (dateWithinRangeRule) {
          s4lConnector.saveFormData(KeystoreKeys.backLinkNewGeoMarket,
            routes.WhatWillUseForController.show().toString())
          Future.successful(Redirect(routes.NewGeographicalMarketController.show()))
        }
        else subsidiariesCheck(hc, hasSub)
      }
      case Some(rfi) if rfi.hadPreviousRFI == Constants.StandardRadioButtonYesValue => {
        // subsequent scheme
        if (dateWithinRangeRule) Future.successful(Redirect(routes.UsedInvestmentReasonBeforeController.show()))
        else subsidiariesCheck(hc, hasSub)
      }

      case None => Future.successful(Redirect(routes.HadPreviousRFIController.show()))
    }
  }

}
