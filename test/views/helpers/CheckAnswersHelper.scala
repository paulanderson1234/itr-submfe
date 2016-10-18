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

package views.helpers

import common.{Constants, KeystoreKeys}
import models._
import org.mockito.Matchers
import org.mockito.Mockito._

import scala.concurrent.Future

trait CheckAnswersHelper extends ViewTestHelper {

  val hadPreviousRFIModelYes = HadPreviousRFIModel(Constants.StandardRadioButtonYesValue)
  val hadPreviousRFIModelNo = HadPreviousRFIModel(Constants.StandardRadioButtonNoValue)
  val proposedInvestmentModel = ProposedInvestmentModel(5000000)
  val whatWillUseForModel = WhatWillUseForModel("Research and development")
  val usedInvestmentReasonBeforeModel = UsedInvestmentReasonBeforeModel(Constants.StandardRadioButtonYesValue)
  val previousBeforeDOFCSModel = PreviousBeforeDOFCSModel("Test")
  val newGeographicalMarketModel = NewGeographicalMarketModel(Constants.StandardRadioButtonYesValue)
  val newProductModel = NewProductModel(Constants.StandardRadioButtonYesValue)
  val subsidiariesSpendingInvestmentModel = SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonYesValue)
  val subsidiariesNinetyOwnedModel = SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonNoValue)
  val investmentGrowModel = InvestmentGrowModel("At vero eos et accusamusi et iusto odio dignissimos ducimus qui blanditiis praesentium " +
    "voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique " +
    "sunt in culpa qui officia deserunt mollitia animi, tid est laborum etttt dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. " +
    "Nam libero tempore, cum soluta nobis est eligendi optio cumque nihili impedit quo minus id quod maxime placeat facere possimus")
  val contactDetailsModel = ContactDetailsModel("Fred", "Smith", "01952 555666", "fredsmith@hotmail.com")
  val yourCompanyNeedModel = YourCompanyNeedModel("AA")
  val taxpayerReferenceModel = TaxpayerReferenceModel("1234567891012")
  val registeredAddressModel = RegisteredAddressModel("SY26GA")
  val dateOfIncorporationModel = DateOfIncorporationModel(Some(20), Some(4), Some(1990))
  val natureOfBusinessModel = NatureOfBusinessModel("Creating new products")
  val commercialSaleModelYes = CommercialSaleModel(Constants.StandardRadioButtonYesValue, Some(4), Some(8), Some(1995))
  val commercialSaleModelNo = CommercialSaleModel(Constants.StandardRadioButtonNoValue, None, None, None)
  val isKnowledgeIntensiveModelYes = IsKnowledgeIntensiveModel(Constants.StandardRadioButtonYesValue)
  val isKnowledgeIntensiveModelNo = IsKnowledgeIntensiveModel(Constants.StandardRadioButtonNoValue)
  val operatingCostsModel = OperatingCostsModel("28976", "12348", "77725", "99883", "23321", "65436")
  val percentageStaffWithMastersModel = PercentageStaffWithMastersModel(Constants.StandardRadioButtonYesValue)
  val tenYearPlanModelYes = TenYearPlanModel(Constants.StandardRadioButtonYesValue, Some("At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium " +
    "voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique " +
    "sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. " +
    "Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus"))
  val tenYearPlanModelNo = TenYearPlanModel(Constants.StandardRadioButtonNoValue, None)
  val subsidiariesModel = SubsidiariesModel(Constants.StandardRadioButtonYesValue)

  def previousSchemeSetup(hadPreviousRFIModel: Option[HadPreviousRFIModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hadPreviousRFIModel))
  }

  def investmentSetup(proposedInvestmentModel: Option[ProposedInvestmentModel] = None, whatWillUseForModel: Option[WhatWillUseForModel] = None,
                      usedInvestmentReasonBeforeModel: Option[UsedInvestmentReasonBeforeModel] = None,
                      previousBeforeDOFCSModel: Option[PreviousBeforeDOFCSModel] = None,
                      newGeographicalMarketModel: Option[NewGeographicalMarketModel] = None, newProductModel: Option[NewProductModel] = None,
                      subsidiariesSpendingInvestmentModel: Option[SubsidiariesSpendingInvestmentModel] = None,
                      subsidiariesNinetyOwnedModel: Option[SubsidiariesNinetyOwnedModel] = None,
                      investmentGrowModel: Option[InvestmentGrowModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(proposedInvestmentModel))
    when(mockS4lConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(whatWillUseForModel))
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

}
