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

package views.helpers

import common.KeystoreKeys
import models._
import org.mockito.Matchers
import org.mockito.Mockito._

import scala.concurrent.Future

trait CheckAnswersSpec extends ViewSpec {

  def previousRFISetup(hadPreviousRFIModel: Option[HadPreviousRFIModel] = None,
                       previousSchemes: Option[Vector[PreviousSchemeModel]] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hadPreviousRFIModel))
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousSchemes))
  }

  def investmentSetup(proposedInvestmentModel: Option[ProposedInvestmentModel] = None,
                      usedInvestmentReasonBeforeModel: Option[UsedInvestmentReasonBeforeModel] = None,
                      previousBeforeDOFCSModel: Option[PreviousBeforeDOFCSModel] = None,
                      newGeographicalMarketModel: Option[NewGeographicalMarketModel] = None, newProductModel: Option[NewProductModel] = None,
                      subsidiariesSpendingInvestmentModel: Option[SubsidiariesSpendingInvestmentModel] = None,
                      subsidiariesNinetyOwnedModel: Option[SubsidiariesNinetyOwnedModel] = None,
                      investmentGrowModel: Option[InvestmentGrowModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(proposedInvestmentModel))
    when(mockS4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(usedInvestmentReasonBeforeModel))
    when(mockS4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousBeforeDOFCSModel))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(newGeographicalMarketModel))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(newProductModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesSpendingInvestmentModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesNinetyOwnedModel))
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(investmentGrowModel))
  }

  def contactDetailsSetup(contactDetailsModel: Option[ContactDetailsModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(contactDetailsModel))
  }

  def contactAddressSetup(contactAddressModel: Option[AddressModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(contactAddressModel))
  }

  def companyDetailsSetup(yourCompanyNeedModel: Option[YourCompanyNeedModel] = None, taxpayerReferenceModel: Option[TaxpayerReferenceModel] = None,
                          registeredAddressModel: Option[RegisteredAddressModel] = None, dateOfIncorporationModel: Option[DateOfIncorporationModel] = None,
                          natureOfBusinessModel: Option[NatureOfBusinessModel] = None, commercialSaleModel: Option[CommercialSaleModel] = None,
                          isKnowledgeIntensiveModel: Option[IsKnowledgeIntensiveModel] = None, operatingCostsModel: Option[OperatingCostsModel] = None,
                          percentageStaffWithMastersModel: Option[PercentageStaffWithMastersModel] = None, tenYearPlanModel: Option[TenYearPlanModel] = None,
                          subsidiariesModel: Option[SubsidiariesModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(yourCompanyNeedModel))
    when(mockS4lConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(taxpayerReferenceModel))
    when(mockS4lConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(registeredAddressModel))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(dateOfIncorporationModel))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(natureOfBusinessModel))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(commercialSaleModel))
    when(mockS4lConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(isKnowledgeIntensiveModel))
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(operatingCostsModel))
    when(mockS4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(percentageStaffWithMastersModel))
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(tenYearPlanModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(subsidiariesModel))
  }

  def seisInvestmentSetup(proposedInvestmentModel: Option[ProposedInvestmentModel] = None,
                          subsidiariesSpendingInvestmentModel: Option[SubsidiariesSpendingInvestmentModel] = None,
                          subsidiariesNinetyOwnedModel: Option[SubsidiariesNinetyOwnedModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(proposedInvestmentModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesSpendingInvestmentModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesNinetyOwnedModel))
  }

  def seisCompanyDetailsSetup(registeredAddressModel: Option[RegisteredAddressModel] = None,
                              dateOfIncorporationModel: Option[DateOfIncorporationModel] = None,
                              natureOfBusinessModel: Option[NatureOfBusinessModel] = None,
                              subsidiariesModel: Option[SubsidiariesModel] = None,
                              tradeStartDateModel: Option[TradeStartDateModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(registeredAddressModel))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(dateOfIncorporationModel))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(natureOfBusinessModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(subsidiariesModel))
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(tradeStartDateModel))
  }

  def tradeStartDateSetup(tradeStartDateModel: Option[TradeStartDateModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(tradeStartDateModel))
  }

  def isSeisInEligibleSetup(eisSeisProcessingModel: Option[EisSeisProcessingModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[EisSeisProcessingModel](Matchers.eq(KeystoreKeys.eisSeisProcessingModel))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisProcessingModel))
  }

}
