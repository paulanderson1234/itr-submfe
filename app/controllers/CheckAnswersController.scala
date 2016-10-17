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
import models._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.checkAndSubmit.CheckAnswers

import scala.concurrent.Future

object CheckAnswersController extends CheckAnswersController{
  val s4lConnector: S4LConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait CheckAnswersController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val s4lConnector: S4LConnector

  def checkAnswersModel(implicit headerCarrier: HeaderCarrier) : Future[CheckAnswersModel] = for {
    yourCompanyNeed <- s4lConnector.fetchAndGetFormData[YourCompanyNeedModel](KeystoreKeys.yourCompanyNeed)
    taxPayerReference <- s4lConnector.fetchAndGetFormData[TaxpayerReferenceModel](KeystoreKeys.taxpayerReference)
    registeredAddress <- s4lConnector.fetchAndGetFormData[RegisteredAddressModel](KeystoreKeys.registeredAddress)
    dateOfIncorporation <- s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
    natureOfBusiness <- s4lConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness)
    commercialSale <- s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
    isKnowledgeIntensive <- s4lConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](KeystoreKeys.isKnowledgeIntensive)
    operatingCosts <- s4lConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts)
    percentageStaffWithMasters <- s4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters)
    tenYearPlan <- s4lConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan)
    subsidiaries <- s4lConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
    hadPreviousRFI <- s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
    proposedInvestment <- s4lConnector.fetchAndGetFormData[ProposedInvestmentModel](KeystoreKeys.proposedInvestment)
    whatWillUseFor <- s4lConnector.fetchAndGetFormData[WhatWillUseForModel](KeystoreKeys.whatWillUseFor)
    usedInvestmentReasonBefore <- s4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](KeystoreKeys.usedInvestmentReasonBefore)
    previousBeforeDOFCS <- s4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS)
    newGeographicalMarket <- s4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket)
    newProduct <- s4lConnector.fetchAndGetFormData[NewProductModel](KeystoreKeys.newProduct)
    subsidiariesSpendingInvestment <- s4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment)
    subsidiariesNinetyOwned <- s4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](KeystoreKeys.subsidiariesNinetyOwned)
    contactDetails <- s4lConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
    investmentGrowModel <- s4lConnector.fetchAndGetFormData[InvestmentGrowModel](KeystoreKeys.investmentGrow)

  }yield new CheckAnswersModel(yourCompanyNeed,taxPayerReference,registeredAddress,dateOfIncorporation
    ,natureOfBusiness,commercialSale,isKnowledgeIntensive,operatingCosts
    ,percentageStaffWithMasters,tenYearPlan,subsidiaries,hadPreviousRFI,proposedInvestment,whatWillUseFor
    ,usedInvestmentReasonBefore,previousBeforeDOFCS,newGeographicalMarket,newProduct,subsidiariesSpendingInvestment,
    subsidiariesNinetyOwned,contactDetails,investmentGrowModel)


  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    checkAnswersModel.flatMap(checkAnswer => Future.successful(Ok(CheckAnswers(checkAnswer))))
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Redirect(routes.AcknowledgementController.show()))
  }


}
