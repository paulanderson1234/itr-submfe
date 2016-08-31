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

import common.KeystoreKeys
import connectors.KeystoreConnector
import controllers.predicates.ValidActiveSession
import forms.WhatWillUseForForm._
import models._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc.{Action, Result}
import utils.Validation
import views.html.investment.WhatWillUseFor
import common.Constants
import controllers.Helpers.KnowledgeIntensiveHelper
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.util.{Failure, Success}

object WhatWillUseForController extends WhatWillUseForController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait WhatWillUseForController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](KeystoreKeys.whatWillUseFor).map {
      case Some(data) => Ok(WhatWillUseFor(whatWillUseForForm.fill(data)))
      case None => Ok(WhatWillUseFor(whatWillUseForForm))
    }
  }

  val submit = Action.async { implicit request =>

    def calcRoute(prevRFI: Option[HadPreviousRFIModel], comSale: Option[CommercialSaleModel],
                  HasSub: Option[SubsidiariesModel], kIFlag: Option[IsKnowledgeIntensiveModel],
                  operatingCosts: Option[OperatingCostsModel], date: Option[DateOfIncorporationModel],
                  percentageStaffMasters: Option[PercentageStaffWithMastersModel], tenYearPlan: Option[TenYearPlanModel]): Future[Result] = {

      comSale match {
        case Some(comSale) => if (comSale.hasCommercialSale.equals(Constants.StandardRadioButtonYesValue)) {
          prevRFI match {
            case Some(prevRFI) => if (prevRFI.hadPreviousRFI.equals(Constants.StandardRadioButtonYesValue)) {
              Future.successful(Redirect(routes.UsedInvestmentReasonBeforeController.show()))
            }
            else {
              getKIRoute(hc, comSale, HasSub, kIFlag, operatingCosts, date, percentageStaffMasters, tenYearPlan)
            }
            case None => Future.successful(Redirect(routes.HadPreviousRFIController.show()))
          }
        }
        else {subsidiariesCheck(hc, HasSub)}

        case None => Future.successful(Redirect(routes.CommercialSaleController.show()))
      }
    }

    whatWillUseForForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(WhatWillUseFor(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.whatWillUseFor, validFormData)

        for {
          prevRFI <- keyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
          comSale <- keyStoreConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
          hasSub <- keyStoreConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
          kIFlag <- keyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](KeystoreKeys.isKnowledgeIntensive)
          operatingCosts <- keyStoreConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts)
          date <- keyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
          percentageStaffMasters <- keyStoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters)
          tenYearPlan <- keyStoreConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan)
          route <- calcRoute(prevRFI, comSale, hasSub, kIFlag, operatingCosts, date, percentageStaffMasters, tenYearPlan)
        } yield route
      }
    )
  }

  def getAgeLimit(isKI: Boolean): String = {
    if(isKI) Constants.IsKnowledgeIntensiveYears
    else Constants.IsNotKnowledgeIntensiveYears
  }

  def subsidiariesCheck(implicit hc: HeaderCarrier, hasSub: Option[SubsidiariesModel]): Future[Result] = {
    hasSub match {
      case Some(hasSub) => if (hasSub.ownSubsidiaries.equals(Constants.StandardRadioButtonYesValue)) {
        keyStoreConnector.saveFormData(KeystoreKeys.backLinkSubSpendingInvestment, routes.WhatWillUseForController.show().toString())
        Future.successful(Redirect(routes.SubsidiariesSpendingInvestmentController.show()))
      } else {
        keyStoreConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow, routes.WhatWillUseForController.show().toString())
        Future.successful(Redirect(routes.InvestmentGrowController.show()))
      }
      case None => Future.successful(Redirect(routes.SubsidiariesController.show()))
    }
  }

  def checkKI(kIFlag: IsKnowledgeIntensiveModel, operatingCosts: OperatingCostsModel,
  date: Option[DateOfIncorporationModel], percentageStaffMasters: Option[PercentageStaffWithMastersModel],
  tenYearPlan: Option[TenYearPlanModel]) : Boolean = {

    if(kIFlag.isKnowledgeIntensive == Constants.StandardRadioButtonYesValue) {
      if (KnowledgeIntensiveHelper.checkRAndDCosts(operatingCosts)) {
        if (KnowledgeIntensiveHelper.validateInput(date, percentageStaffMasters)) {
          if (KnowledgeIntensiveHelper.checkKI(date.get, percentageStaffMasters.get, tenYearPlan.isDefined, tenYearPlan)) {
            true
          } else false
        } else false
      } else false
    } else false
  }

  def getKIRoute(implicit hc: HeaderCarrier, comSale: CommercialSaleModel, HasSub: Option[SubsidiariesModel],
                 kIFlag: Option[IsKnowledgeIntensiveModel], operatingCosts: Option[OperatingCostsModel],
                 date: Option[DateOfIncorporationModel], percentageStaffMasters: Option[PercentageStaffWithMastersModel],
                 tenYearPlan: Option[TenYearPlanModel]): Future[Result] = kIFlag match {

    case Some(kIFlag) => {
      operatingCosts match {
        case Some(operatingCosts) => {
          if (Validation.checkAgeRule(comSale.commercialSaleDay.get, comSale.commercialSaleMonth.get, comSale
            .commercialSaleYear.get, getAgeLimit(checkKI(kIFlag, operatingCosts, date, percentageStaffMasters, tenYearPlan)))) {
            keyStoreConnector.saveFormData(KeystoreKeys.backLinkNewGeoMarket, routes.WhatWillUseForController.show().toString())
            Future.successful(Redirect(routes.NewGeographicalMarketController.show()))
          }
          else subsidiariesCheck(hc, HasSub)
        }
      case None =>  Future.successful(Redirect(routes.OperatingCostsController.show()))
      }
    }
    case None =>
      keyStoreConnector.saveFormData(KeystoreKeys.backLinkSubSpendingInvestment, routes.WhatWillUseForController.show().toString())
      Future.successful(Redirect(routes.SubsidiariesSpendingInvestmentController.show()))
  }
}
