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

package fixtures

import common.{Constants, KeystoreKeys}
import connectors.S4LConnector
import models.registration.RegistrationDetailsModel
import models.{KiProcessingModel, _}
import org.mockito.Matchers
import org.mockito.Mockito._
import auth.AuthEnrolledTestController.{INTERNAL_SERVER_ERROR => _, OK => _, SEE_OTHER => _, _}
import models.submission._
import services.RegistrationDetailsService

import scala.concurrent.Future

//noinspection ScalaStyle
trait SubmissionFixture {

  def setUpMocks(mockS4lConnector: S4LConnector) {

    // mandatory
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(kiProcModelValid)))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(natureOfBusinessValid)))
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(contactDetailsValid)))
    when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(proposedInvestmentValid)))
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(investmentGrowValid)))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(dateOfIncorporationValid)))
    when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(fullCorrespondenceAddress)))


    // potentially mandatory
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(subsidiariesSpendInvestValid)))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(subsidiariesNinetyOwnedValid)))
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(previousSchemesValid)))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(commercialSaleValid)))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(newGeographicalMarketValid)))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(newProductValid)))
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(tenYearPlanValid)))
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(operatingCostsValid)))
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(turnoverCostsValid)))
  }

  def setUpMocksRegistrationService(mockRegistrationService: RegistrationDetailsService): Unit = {
    when(mockRegistrationService.getRegistrationDetails(Matchers.eq(tavcReferenceId))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(registrationDetailsModel)))
  }

  def setUpMocksMinimumRequiredModels(mockS4lConnector: S4LConnector) {


    // mandatory minimum
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(kiProcModelValid)))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(natureOfBusinessValid)))
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(contactDetailsValid)))
    when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(proposedInvestmentValid)))
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(investmentGrowValid)))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(dateOfIncorporationValid)))
    when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(fullCorrespondenceAddress)))

    // can be empty to pass
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(turnoverCostsValid)))
  }

  def setUpMocksTestMinimumRequiredModels(mockS4lConnector: S4LConnector, mockRegistrationService: RegistrationDetailsService,
                                          kiModel: Option[KiProcessingModel],
                                          natureBusiness: Option[NatureOfBusinessModel],
                                          contactDetails: Option[ContactDetailsModel],
                                          proposedInvestment: Option[ProposedInvestmentModel],
                                          investGrow: Option[InvestmentGrowModel],
                                          dateIncorp: Option[DateOfIncorporationModel],
                                          contactAddress: Option[AddressModel],
                                          returnRegistrationDetails: Boolean
                                         )
  {

    // mandatory minimum
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(kiModel.nonEmpty) Future.successful(Option(kiModel.get)) else Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(natureBusiness.nonEmpty) Future.successful(Option(natureBusiness.get)) else Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(contactDetails.nonEmpty) Future.successful(Option(contactDetails.get)) else Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(proposedInvestment.nonEmpty) Future.successful(Option(proposedInvestment.get)) else Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(investGrow.nonEmpty) Future.successful(Option(investGrow.get)) else Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(dateIncorp.nonEmpty) Future.successful(Option(dateIncorp.get)) else Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(contactAddress.nonEmpty) Future.successful(Option(contactAddress.get)) else Future.successful(None))

      when(mockRegistrationService.getRegistrationDetails(Matchers.eq(tavcReferenceId))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(if(returnRegistrationDetails) Future.successful(Option(registrationDetailsModel)) else Future.successful(None))


    // can be empty to pass
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(turnoverCostsValid)))
  }

  val fullCorrespondenceAddress: AddressModel = AddressModel(addressline1 = "line 1",
    addressline2 = "Line 2", addressline3 = Some("Line 3"), addressline4 = Some("Line 4"),
    postcode = Some("TF1 4NY"), countryCode = "GB")

  val registrationDetailsModel = RegistrationDetailsModel("Company ltd", fullCorrespondenceAddress)

  val fullContactDetailsModel: ContactDetailsModel = ContactDetailsModel(forename = "Fred",
    surname = "Flinsstone", telephoneNumber = Some("01952 255899"), mobileNumber = None, email = "rubble@jurassic.com")

  val schemeTypesEIS: SchemeTypesModel = SchemeTypesModel(eis = true, seis = false, vct = false, sitr = false)
  val schemeTypesSEIS: SchemeTypesModel = SchemeTypesModel(eis = false, seis = true, vct = false, sitr = false)
  val testAgentRef = "AARN1234567"
  val tavcReferenceId = "XATAVC000123456"

  val marketInfo = SubmitMarketInfoModel(newGeographicalMarketModel = NewGeographicalMarketModel(Constants.StandardRadioButtonNoValue),
    newProductModel = NewProductModel(Constants.StandardRadioButtonYesValue))

   val opcostFull = OperatingCostsModel(operatingCosts1stYear = "101", operatingCosts2ndYear = "102",
    operatingCosts3rdYear = "103", rAndDCosts1stYear = "201", rAndDCosts2ndYear = "202", rAndDCosts3rdYear = "203",
     firstYear = "2005", secondYear = "2004", thirdYear = "2003")

  val costsFull = utils.Converters.operatingCostsToList(opcostFull)

  val turnover = List(TurnoverCostModel("2003", turnover = CostModel("66")),
    TurnoverCostModel("2004", turnover = CostModel("67")),
    TurnoverCostModel("2004", turnover = CostModel("68")),
    TurnoverCostModel("2004", turnover = CostModel("69")),
    TurnoverCostModel("2005", turnover = CostModel("70")))

  val dateOfIncorporationModel = DateOfIncorporationModel(day = Some(5), month = Some(6), year = Some(2007))
  val startDateModelModelYes = TradeStartDateModel(tradeStartDay = Some(5), tradeStartMonth = Some(6),
    tradeStartYear = Some(2007), hasTradeStartDate = Constants.StandardRadioButtonYesValue)
  val startDateModelModelNo = TradeStartDateModel(tradeStartDay = Some(5), tradeStartMonth = Some(6),
    tradeStartYear = Some(2007), hasTradeStartDate = Constants.StandardRadioButtonNoValue)

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
    otherSchemeName = None),
    PreviousSchemeModel(schemeTypeDesc = Constants.schemeTypeEis, investmentAmount = 5000,
      day = Some(2),
      month = Some(3),
      year = Some(2003),
      processingId = None,
      investmentSpent = Some(20),
      otherSchemeName = None),
    PreviousSchemeModel(schemeTypeDesc = Constants.schemeTypeOther, investmentAmount = 6000,
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

  val tradeStartDateModelYes = TradeStartDateModel(Constants.StandardRadioButtonYesValue, Some(1), Some(1), Some(2001))
  val tradeStartDateModelNo = TradeStartDateModel(Constants.StandardRadioButtonNoValue, None, None, None)

  val model = AdvancedAssuranceSubmissionType(
    agentReferenceNumber = Some(testAgentRef),
    acknowledgementReference = Some("AARN1234567"),
    whatWillUseForModel = Some(WhatWillUseForModel(None)),
    natureOfBusinessModel = NatureOfBusinessModel("Some nature of business description"),
    contactDetailsModel = fullContactDetailsModel,
    correspondenceAddress = fullCorrespondenceAddress,
    schemeTypes = schemeTypesEIS,
    marketInfo = Some(marketInfo),
    dateTradeCommenced = tradeStartDateModelYes.toDate,
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
  val contactDetailsValid = ContactDetailsModel("fred", "Smith", Some("01952 245666"), None, "fred@hotmail.com")
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
  val operatingCostsValid = OperatingCostsModel("12", "13", "14", "15", "16", "17", "2005", "2004", "2003")
  val turnoverCostsValid = AnnualTurnoverCostsModel("12", "13", "14", "15", "16", "2003", "2004", "2005", "2006", "2007")
}
