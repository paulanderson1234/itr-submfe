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
import models._
import play.api.mvc.Action
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.checkAndSubmit.CheckAnswers

import scala.concurrent.Future

object CheckAnswersController extends CheckAnswersController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait CheckAnswersController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  def checkAnswersModel(implicit headerCarrier: HeaderCarrier) : Future[CheckAnswersModel] = for {
    yourCompanyNeed <- keyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](KeystoreKeys.yourCompanyNeed)
    taxPayerReference <- keyStoreConnector.fetchAndGetFormData[TaxpayerReferenceModel](KeystoreKeys.taxpayerReference)
    registeredAddress <- keyStoreConnector.fetchAndGetFormData[RegisteredAddressModel](KeystoreKeys.registeredAddress)
    dateOfIncorporation <- keyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
    natureOfBusiness <- keyStoreConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness)
    commercialSale <- keyStoreConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
    isKnowledgeIntensive <- keyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](KeystoreKeys.isKnowledgeIntensive)
    operatingCosts <- keyStoreConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts)
    percentageStaffWithMasters <- keyStoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters)
    tenYearPlan <- keyStoreConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan)
    subsidiaries <- keyStoreConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
    hadPreviousRFI <- keyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
    proposedInvestment <- keyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel](KeystoreKeys.proposedInvestment)
    whatWillUseFor <- keyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](KeystoreKeys.whatWillUseFor)
    usedInvestmentReasonBefore <- keyStoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](KeystoreKeys.usedInvestmentReasonBefore)
    previousBeforeDOFCS <- keyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS)
    newGeographicalMarket <- keyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket)
    subsidiariesSpendingInvestment <- keyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment)
    subsidiariesNinetyOwned <- keyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](KeystoreKeys.subsidiariesNinetyOwned)
    investmentGrowModel <- keyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](KeystoreKeys.investmentGrow)

  }yield new CheckAnswersModel(yourCompanyNeed,taxPayerReference,registeredAddress,dateOfIncorporation
    ,natureOfBusiness,commercialSale,isKnowledgeIntensive,operatingCosts
    ,percentageStaffWithMasters,tenYearPlan,subsidiaries,hadPreviousRFI,proposedInvestment,whatWillUseFor
    ,usedInvestmentReasonBefore,previousBeforeDOFCS,newGeographicalMarket,subsidiariesSpendingInvestment
    ,subsidiariesNinetyOwned,investmentGrowModel)


  val show = ValidateSession.async { implicit request =>
    checkAnswersModel.flatMap(checkAnswer => Future.successful(Ok(CheckAnswers(checkAnswer))))
  }

  val submit = Action.async { implicit request =>
    Future.successful(Redirect(routes.CheckAnswersController.show()))
  }


}
