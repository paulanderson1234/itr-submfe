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

package fixtures

import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
import models.{KiProcessingModel, _}
import org.mockito.Matchers
import org.mockito.Mockito._
import auth.AuthEnrolledTestController.{INTERNAL_SERVER_ERROR => _, OK => _, SEE_OTHER => _, _}
import models.submission._

import scala.concurrent.Future

//noinspection ScalaStyle
trait SubmissionFixture {

  def setUpMocks(mockKeyStoreConnector: KeystoreConnector) {

    // mandatory
    when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(kiProcModelValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(natureOfBusinessValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(contactDetailsValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(proposedInvestmentValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(investmentGrowValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(dateOfIncorporationValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(fullCorrespondenceAddress)))

    // potentially mandatory
    when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(subsidiariesSpendInvestValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(subsidiariesNinetyOwnedValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(previousSchemesValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(commercialSaleValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(newGeographicalMarketValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(newProductValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(tenYearPlanValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(operatingCostsValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(turnoverCostsValid)))
  }

  def setUpMocksMinimumRequiredModels(mockKeyStoreConnector: KeystoreConnector) {

    // mandatory minimum
    when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(kiProcModelValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(natureOfBusinessValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(contactDetailsValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(proposedInvestmentValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(investmentGrowValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(dateOfIncorporationValid)))
    when(mockKeyStoreConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(fullCorrespondenceAddress)))

    // can be empty to pass
    when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(turnoverCostsValid)))
  }

  def setUpMocksTestMinimumRequiredModels(mockKeyStoreConnector: KeystoreConnector,
                                          kiModel: Option[KiProcessingModel],
                                          natureBusiness: Option[NatureOfBusinessModel],
                                          contactDetails: Option[ContactDetailsModel],
                                          proposedInvestment: Option[ProposedInvestmentModel],
                                          investGrow: Option[InvestmentGrowModel],
                                          dateIncorp: Option[DateOfIncorporationModel],
                                          contactAddress: Option[AddressModel]
                                         )
  {

    // mandatory minimum
    when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
      .thenReturn( if(kiModel.nonEmpty) Future.successful(Option(kiModel.get)) else Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
      .thenReturn( if(natureBusiness.nonEmpty) Future.successful(Option(natureBusiness.get)) else Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any()))
      .thenReturn( if(contactDetails.nonEmpty) Future.successful(Option(contactDetails.get)) else Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
      .thenReturn( if(proposedInvestment.nonEmpty) Future.successful(Option(proposedInvestment.get)) else Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
      .thenReturn( if(investGrow.nonEmpty) Future.successful(Option(investGrow.get)) else Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
      .thenReturn( if(dateIncorp.nonEmpty) Future.successful(Option(dateIncorp.get)) else Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any()))
      .thenReturn( if(contactAddress.nonEmpty) Future.successful(Option(contactAddress.get)) else Future.successful(None))

    // can be empty to pass
    when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockKeyStoreConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(turnoverCostsValid)))
  }

  val fullCorrespondenceAddress: AddressModel = AddressModel(addressline1 = "line 1",
    addressline2 = "Line 2", addressline3 = Some("Line 3"), addressline4 = Some("Line 4"),
    postcode = Some("TF1 4NY"), countryCode = "GB")

  val fullContactDetailsModel: ContactDetailsModel = ContactDetailsModel(forename = "Fred",
    surname = "Flinsstone", telephoneNumber = "01952 255899", email = "rubble@jurassic.com")

  val schemeTypes: SchemeTypesModel = SchemeTypesModel(eis = true, seis = false, vct = false, sitr = false)
  val testAgentRef = "AARN1234567"
  val tavcReferenceId = "AA1234567890000"

  val marketInfo = SubmitMarketInfoModel(newGeographicalMarketModel = NewGeographicalMarketModel(Constants.StandardRadioButtonNoValue),
    newProductModel = NewProductModel(Constants.StandardRadioButtonYesValue))

   val opcostFull = OperatingCostsModel(operatingCosts1stYear = "101", operatingCosts2ndYear = "102",
    operatingCosts3rdYear = "103", rAndDCosts1stYear = "201", rAndDCosts2ndYear = "202", rAndDCosts3rdYear = "203")

  val costsFull = utils.Converters.operatingCostsToList(opcostFull, 2005)

  val turnover = List(TurnoverCostModel("2003", turnover = CostModel("66")),
    TurnoverCostModel("2004", turnover = CostModel("67")),
    TurnoverCostModel("2004", turnover = CostModel("68")),
    TurnoverCostModel("2004", turnover = CostModel("69")),
    TurnoverCostModel("2005", turnover = CostModel("70")))

  val dateOfIncorporationModel = DateOfIncorporationModel(day = Some(5), month = Some(6), year = Some(2007))

  val subsidiaryPerformingTradeMinimumReq = SubsidiaryPerformingTradeModel(ninetyOwnedModel = SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonYesValue),
    organisationName = "Made up test subsidiary org name")
  val subsidiaryPerformingTradeWithAddress = SubsidiaryPerformingTradeModel(ninetyOwnedModel =
    SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonYesValue), organisationName = "Made up test subsidiary org name",
    companyAddress = Some(fullCorrespondenceAddress))

  val subsidiaryPerformingTradeWithFull = SubsidiaryPerformingTradeModel(ninetyOwnedModel =
    SubsidiariesNinetyOwnedModel("true"), organisationName = "Made up test subsidiary org name",
    companyAddress = Some(fullCorrespondenceAddress), ctUtr = Some("1234567891"), crn = Some("555589852"))

  val previousSchemesFull = Vector(PreviousSchemeModel(schemeTypeDesc = Constants.schemeTypeEis, investmentAmount = 2000,
    day = Some(1),
    month = Some(2),
    year = Some(2004),
    processingId = None,
    investmentSpent = Some(19),
    otherSchemeName = Some("Other 1")),
    PreviousSchemeModel(schemeTypeDesc = Constants.schemeTypeEis, investmentAmount = 5000,
      day = Some(2),
      month = Some(3),
      year = Some(2003),
      processingId = None,
      investmentSpent = Some(20),
      otherSchemeName = Some("Other 2")),
    PreviousSchemeModel(schemeTypeDesc = Constants.schemeTypeEis, investmentAmount = 6000,
      day = Some(4),
      month = Some(5),
      year = Some(2002),
      processingId = None,
      investmentSpent = Some(21),
      otherSchemeName = Some("Other 3"))
  )

  val organisationFull = OrganisationDetailsModel(utr = Some("1234567891"), organisationName = "my org name",
    chrn = Some("2222222222"), startDate = dateOfIncorporationModel, firstDateOfCommercialSale = Some("2009-04-01"),
    ctUtr = Some("5555555555"), crn = Some("crnvalue"), companyAddress = Some(fullCorrespondenceAddress),
    previousRFIs = Some(previousSchemesFull.toList))


  val model = AdvancedAssuranceSubmissionType(
    agentReferenceNumber = Some(testAgentRef),
    acknowledgementReference = Some("AARN1234567"),
    whatWillUseForModel = Some(WhatWillUseForModel(None)),
    natureOfBusinessModel = NatureOfBusinessModel("Some nature of business description"),
    contactDetailsModel = fullContactDetailsModel,
    correspondenceAddress = fullCorrespondenceAddress,
    schemeTypes = schemeTypes,
    marketInfo = Some(marketInfo),
    annualCosts = Some(costsFull),
    annualTurnover = Some(turnover),
    proposedInvestmentModel = ProposedInvestmentModel(250000),
    investmentGrowModel = InvestmentGrowModel("It will help me invest in new equipment and R&D"),
    knowledgeIntensive = Some(KiModel(skilledEmployeesConditionMet = true, innovationConditionMet = Some("reason met"), kiConditionMet = true)),
    subsidiaryPerformingTrade = Some(subsidiaryPerformingTradeWithFull),
    organisationDetails = organisationFull
  )

  val fullSubmissionSourceData = Submission(model)

  val kiProcModelValid = KiProcessingModel(companyAssertsIsKi = Some(true), dateConditionMet = Some(true), hasPercentageWithMasters = Some(true), costsConditionMet = Some(true))
  val kiProcModelValidAssertNo = KiProcessingModel(companyAssertsIsKi = Some(false), dateConditionMet = Some(true), hasPercentageWithMasters = Some(true), costsConditionMet = Some(true))
  val whatWillUseForValid = None
  val natureOfBusinessValid = NatureOfBusinessModel("Technology supplier")
  val contactDetailsValid = ContactDetailsModel("fred", "Smith", "01952 245666", "fred@hotmail.com")
  val proposedInvestmentValid = ProposedInvestmentModel(2000)
  val investmentGrowValid = InvestmentGrowModel("It will be used to pay for R&D")
  val dateOfIncorporationValid = DateOfIncorporationModel(Some(2), Some(3), Some(2012))

  // potentially optional or required
  val subsidiariesSpendInvestValid = SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonYesValue)
  val subsidiariesNinetyOwnedValid = SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonYesValue)
  val previousSchemesValid = previousSchemesFull
  val commercialSaleValid = CommercialSaleModel(Constants.StandardRadioButtonYesValue, Some(12), Some(5), Some(2011))
  val newGeographicalMarketValid = NewGeographicalMarketModel(Constants.StandardRadioButtonYesValue)
  val newProductValid = NewProductModel(Constants.StandardRadioButtonYesValue)
  val tenYearPlanValid = TenYearPlanModel(Constants.StandardRadioButtonYesValue, Some("To borrow to invest as in business plan"))
  val operatingCostsValid = OperatingCostsModel("12", "13", "14", "15", "16", "17")
  val turnoverCostsValid = AnnualTurnoverCostsModel("12", "13", "14", "15", "16")
}
