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

/**
  * Copyright 2016 HM Revenue & Customs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package views

import java.util.UUID

import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
import controllers.{CheckAnswersController, routes}
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class CheckAnswersCompanyDetailsSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach {

  val mockKeystoreConnector = mock[KeystoreConnector]

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
  val hadPreviousRFIModel = HadPreviousRFIModel("")
  val proposedInvestmentModel = ProposedInvestmentModel(0)
  val whatWillUseForModel = WhatWillUseForModel("")
  val usedInvestmentReasonBeforeModel = UsedInvestmentReasonBeforeModel("")
  val previousBeforeDOFCSModel = PreviousBeforeDOFCSModel("")
  val newGeographicalMarketModel = NewGeographicalMarketModel("")
  val newProductModel = NewProductModel("")
  val subsidiariesSpendingInvestmentModel = SubsidiariesSpendingInvestmentModel("")
  val subsidiariesNinetyOwnedModel = SubsidiariesNinetyOwnedModel("")
  val investmentGrowModel = InvestmentGrowModel("")


  class SetupPage {

    val controller = new CheckAnswersController {
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  override def beforeEach() {
    reset(mockKeystoreConnector)
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(yourCompanyNeedModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(taxpayerReferenceModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(registeredAddressModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(dateOfIncorporationModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(natureOfBusinessModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(commercialSaleModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(isKnowledgeIntensiveModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(operatingCostsModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(percentageStaffWithMastersModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(tenYearPlanModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(hadPreviousRFIModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(proposedInvestmentModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(whatWillUseForModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(usedInvestmentReasonBeforeModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(previousBeforeDOFCSModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(newGeographicalMarketModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(newProductModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(subsidiariesSpendingInvestmentModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(subsidiariesNinetyOwnedModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(investmentGrowModel)))

        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description")
      document.getElementById("print-this-page").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.print.text")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Company name
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-question").text() shouldBe
        Messages("summaryQuestion.companyName")
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-answer").text() shouldBe
        Messages("common.notAvailable")
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-link")
       .attr("href") shouldEqual routes.IntroductionController.show().toString()
      //Taxpayer Reference
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-question").text() shouldBe
        Messages("summaryQuestion.utr")
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-answer").text() shouldBe
        taxpayerReferenceModel.utr
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-link")
        .attr("href") shouldEqual routes.TaxpayerReferenceController.show().toString()
      //Registered address
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-question").text() shouldBe
        Messages("summaryQuestion.registeredAddress")
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-answer").text() shouldBe
        registeredAddressModel.postcode
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-link")
        .attr("href") shouldEqual routes.RegisteredAddressController.show().toString()
      //Date of incorporation
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(dateOfIncorporationModel.day.get,dateOfIncorporationModel.month.get,dateOfIncorporationModel.year.get)
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual routes.DateOfIncorporationController.show().toString()
      //Nature of business
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-answer").text() shouldBe
        natureOfBusinessModel.natureofbusiness
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual routes.NatureOfBusinessController.show().toString()
      //Has had commercial sale
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-question").text() shouldBe
        Messages("summaryQuestion.hasCommercialSale")
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-answer").text() shouldBe
        commercialSaleModelYes.hasCommercialSale
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-link")
        .attr("href") shouldEqual routes.CommercialSaleController.show().toString()
      //Date of first commercial sale
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("commercialSaleDate-question").text() shouldBe
        Messages("summaryQuestion.commercialSaleDate")
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("commercialSaleDate-answer").text() shouldBe
        CommercialSaleModel.toDateString(commercialSaleModelYes.commercialSaleDay.get,commercialSaleModelYes.commercialSaleMonth.get,commercialSaleModelYes.commercialSaleYear.get)
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("commercialSaleDate-link")
        .attr("href") shouldEqual routes.CommercialSaleController.show().toString()
      //Is Knowledge Intensive
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("knowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.knowledgeIntensive")
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("knowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("knowledgeIntensive-link")
        .attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show().toString()
      //Operating costs
      document.getElementById("company-details-table").select("tbody").select("tr").get(8).getElementById("operatingCosts-question").text() shouldBe
        Messages("summaryQuestion.operatingCosts")
      document.getElementById("company-details-table").select("tbody").select("tr").get(8).getElementById("operatingCosts-answer").text() should contain
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))
      document.getElementById("company-details-table").select("tbody").select("tr").get(8).getElementById("operatingCosts-link")
        .attr("href") shouldEqual routes.OperatingCostsController.show().toString()
      //R&D costs
      document.getElementById("company-details-table").select("tbody").select("tr").get(9).getElementById("rdCosts-question").text() shouldBe
        Messages("summaryQuestion.rdCosts")
      document.getElementById("company-details-table").select("tbody").select("tr").get(9).getElementById("rdCosts-answer").text() should contain
      OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))
      document.getElementById("company-details-table").select("tbody").select("tr").get(9).getElementById("rdCosts-link")
        .attr("href") shouldEqual routes.OperatingCostsController.show().toString()
      //Percentage of staff with masters
      document.getElementById("company-details-table").select("tbody").select("tr").get(10).getElementById("percentageStaffWithMasters-question").text() shouldBe
        Messages("summaryQuestion.percentageStaffWithMasters")
      document.getElementById("company-details-table").select("tbody").select("tr").get(10).getElementById("percentageStaffWithMasters-answer").text() shouldBe
        PercentageStaffWithMastersModel.staffWithMastersToString(percentageStaffWithMastersModel.staffWithMasters)
      document.getElementById("company-details-table").select("tbody").select("tr").get(10).getElementById("percentageStaffWithMasters-link")
        .attr("href") shouldEqual routes.PercentageStaffWithMastersController.show().toString()
      //Has ten year plan
      document.getElementById("company-details-table").select("tbody").select("tr").get(11).getElementById("tenYearPlan-question").text() shouldBe
        Messages("summaryQuestion.tenYearPlan")
      document.getElementById("company-details-table").select("tbody").select("tr").get(11).getElementById("tenYearPlan-answer").text() shouldBe
        tenYearPlanModelYes.hasTenYearPlan
      document.getElementById("company-details-table").select("tbody").select("tr").get(11).getElementById("tenYearPlan-link")
        .attr("href") shouldEqual routes.TenYearPlanController.show().toString()
      //Ten year plan description
      document.getElementById("company-details-table").select("tbody").select("tr").get(12).getElementById("tenYearPlanDesc-question").text() shouldBe
        Messages("summaryQuestion.tenYearPlanDesc")
      document.getElementById("company-details-table").select("tbody").select("tr").get(12).getElementById("tenYearPlanDesc-answer").text() shouldBe
        tenYearPlanModelYes.tenYearPlanDesc.get
      document.getElementById("company-details-table").select("tbody").select("tr").get(12).getElementById("tenYearPlanDesc-link")
        .attr("href") shouldEqual routes.TenYearPlanController.show().toString()
      //Subsidiaries
      document.getElementById("company-details-table").select("tbody").select("tr").get(13).getElementById("subsidiaries-question").text() shouldBe
        Messages("summaryQuestion.subsidiaries")
      document.getElementById("company-details-table").select("tbody").select("tr").get(13).getElementById("subsidiaries-answer").text() shouldBe
        subsidiariesModel.ownSubsidiaries
      document.getElementById("company-details-table").select("tbody").select("tr").get(13).getElementById("subsidiaries-link")
        .attr("href") shouldEqual routes.SubsidiariesController.show().toString()

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SupportingDocumentsController.show().toString()
    }
  }


  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when an empty set of company detail models are passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))

        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description")
      document.getElementById("print-this-page").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.print.text")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Company name
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-question").text() shouldBe
        Messages("summaryQuestion.companyName")
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-answer").text() shouldBe
        Messages("common.notAvailable")
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-link")
        .attr("href") shouldEqual routes.IntroductionController.show().toString()
      //Taxpayer Reference
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-question").text() shouldBe
        Messages("summaryQuestion.utr")
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-answer").text() shouldBe
        Messages("common.notAvailable")
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-link")
        .attr("href") shouldEqual routes.TaxpayerReferenceController.show().toString()
      //Registered address
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-question").text() shouldBe
        Messages("summaryQuestion.registeredAddress")
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-answer").text() shouldBe
        Messages("common.notAvailable")
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-link")
        .attr("href") shouldEqual routes.RegisteredAddressController.show().toString()
      //Date of incorporation
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-answer").text() shouldBe
        Messages("common.notAvailable")
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual routes.DateOfIncorporationController.show().toString()
      //Nature of business
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-answer").text() shouldBe
        Messages("common.notAvailable")
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual routes.NatureOfBusinessController.show().toString()
      //Has had commercial sale
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-question").text() shouldBe
        Messages("summaryQuestion.hasCommercialSale")
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-answer").text() shouldBe
        Messages("common.notAvailable")
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-link")
        .attr("href") shouldEqual routes.CommercialSaleController.show().toString()
      //Is Knowledge Intensive
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("knowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.knowledgeIntensive")
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("knowledgeIntensive-answer").text() shouldBe
        Messages("common.notAvailable")
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("knowledgeIntensive-link")
        .attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show().toString()
      //Subsidiaries
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("subsidiaries-question").text() shouldBe
        Messages("summaryQuestion.subsidiaries")
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("subsidiaries-answer").text() shouldBe
        Messages("common.notAvailable")
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("subsidiaries-link")
        .attr("href") shouldEqual routes.SubsidiariesController.show().toString()

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SupportingDocumentsController.show().toString()
    }
  }


  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models but a commercial sale has not been made" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(yourCompanyNeedModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(taxpayerReferenceModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(registeredAddressModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(dateOfIncorporationModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(natureOfBusinessModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(commercialSaleModelNo)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(isKnowledgeIntensiveModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(operatingCostsModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(percentageStaffWithMastersModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(tenYearPlanModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(hadPreviousRFIModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(proposedInvestmentModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(whatWillUseForModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(usedInvestmentReasonBeforeModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(previousBeforeDOFCSModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(newGeographicalMarketModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(newProductModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(subsidiariesSpendingInvestmentModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(subsidiariesNinetyOwnedModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(investmentGrowModel)))

        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description")
      document.getElementById("print-this-page").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.print.text")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Company name
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-question").text() shouldBe
        Messages("summaryQuestion.companyName")
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-answer").text() shouldBe
        Messages("common.notAvailable")
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-link")
        .attr("href") shouldEqual routes.IntroductionController.show().toString()
      //Taxpayer Reference
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-question").text() shouldBe
        Messages("summaryQuestion.utr")
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-answer").text() shouldBe
        taxpayerReferenceModel.utr
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-link")
        .attr("href") shouldEqual routes.TaxpayerReferenceController.show().toString()
      //Registered address
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-question").text() shouldBe
        Messages("summaryQuestion.registeredAddress")
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-answer").text() shouldBe
        registeredAddressModel.postcode
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-link")
        .attr("href") shouldEqual routes.RegisteredAddressController.show().toString()
      //Date of incorporation
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(dateOfIncorporationModel.day.get,dateOfIncorporationModel.month.get,dateOfIncorporationModel.year.get)
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual routes.DateOfIncorporationController.show().toString()
      //Nature of business
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-answer").text() shouldBe
        natureOfBusinessModel.natureofbusiness
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual routes.NatureOfBusinessController.show().toString()
      //Has had commercial sale
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-question").text() shouldBe
        Messages("summaryQuestion.hasCommercialSale")
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-answer").text() shouldBe
        commercialSaleModelNo.hasCommercialSale
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-link")
        .attr("href") shouldEqual routes.CommercialSaleController.show().toString()
      //Is Knowledge Intensive
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("knowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.knowledgeIntensive")
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("knowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("knowledgeIntensive-link")
        .attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show().toString()
      //Operating costs
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("operatingCosts-question").text() shouldBe
        Messages("summaryQuestion.operatingCosts")
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("operatingCosts-answer").text() should contain
      OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("operatingCosts-link")
        .attr("href") shouldEqual routes.OperatingCostsController.show().toString()
      //R&D costs
      document.getElementById("company-details-table").select("tbody").select("tr").get(8).getElementById("rdCosts-question").text() shouldBe
        Messages("summaryQuestion.rdCosts")
      document.getElementById("company-details-table").select("tbody").select("tr").get(8).getElementById("rdCosts-answer").text() should contain
      OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))
      document.getElementById("company-details-table").select("tbody").select("tr").get(8).getElementById("rdCosts-link")
        .attr("href") shouldEqual routes.OperatingCostsController.show().toString()
      //Percentage of staff with masters
      document.getElementById("company-details-table").select("tbody").select("tr").get(9).getElementById("percentageStaffWithMasters-question").text() shouldBe
        Messages("summaryQuestion.percentageStaffWithMasters")
      document.getElementById("company-details-table").select("tbody").select("tr").get(9).getElementById("percentageStaffWithMasters-answer").text() shouldBe
        PercentageStaffWithMastersModel.staffWithMastersToString(percentageStaffWithMastersModel.staffWithMasters)
      document.getElementById("company-details-table").select("tbody").select("tr").get(9).getElementById("percentageStaffWithMasters-link")
        .attr("href") shouldEqual routes.PercentageStaffWithMastersController.show().toString()
      //Has ten year plan
      document.getElementById("company-details-table").select("tbody").select("tr").get(10).getElementById("tenYearPlan-question").text() shouldBe
        Messages("summaryQuestion.tenYearPlan")
      document.getElementById("company-details-table").select("tbody").select("tr").get(10).getElementById("tenYearPlan-answer").text() shouldBe
        tenYearPlanModelYes.hasTenYearPlan
      document.getElementById("company-details-table").select("tbody").select("tr").get(10).getElementById("tenYearPlan-link")
        .attr("href") shouldEqual routes.TenYearPlanController.show().toString()
      //Ten year plan description
      document.getElementById("company-details-table").select("tbody").select("tr").get(11).getElementById("tenYearPlanDesc-question").text() shouldBe
        Messages("summaryQuestion.tenYearPlanDesc")
      document.getElementById("company-details-table").select("tbody").select("tr").get(11).getElementById("tenYearPlanDesc-answer").text() shouldBe
        tenYearPlanModelYes.tenYearPlanDesc.get
      document.getElementById("company-details-table").select("tbody").select("tr").get(11).getElementById("tenYearPlanDesc-link")
        .attr("href") shouldEqual routes.TenYearPlanController.show().toString()
      //Subsidiaries
      document.getElementById("company-details-table").select("tbody").select("tr").get(12).getElementById("subsidiaries-question").text() shouldBe
        Messages("summaryQuestion.subsidiaries")
      document.getElementById("company-details-table").select("tbody").select("tr").get(12).getElementById("subsidiaries-answer").text() shouldBe
        subsidiariesModel.ownSubsidiaries
      document.getElementById("company-details-table").select("tbody").select("tr").get(12).getElementById("subsidiaries-link")
        .attr("href") shouldEqual routes.SubsidiariesController.show().toString()

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SupportingDocumentsController.show().toString()
    }
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models but it is not knowledge intensive and therefore should not show KI pages" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(yourCompanyNeedModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(taxpayerReferenceModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(registeredAddressModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(dateOfIncorporationModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(natureOfBusinessModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(commercialSaleModelNo)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(isKnowledgeIntensiveModelNo)))
        when(mockKeystoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(operatingCostsModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(percentageStaffWithMastersModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(tenYearPlanModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(hadPreviousRFIModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(proposedInvestmentModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(whatWillUseForModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(usedInvestmentReasonBeforeModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(previousBeforeDOFCSModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(newGeographicalMarketModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(newProductModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(subsidiariesSpendingInvestmentModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(subsidiariesNinetyOwnedModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(investmentGrowModel)))

        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description")
      document.getElementById("print-this-page").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.print.text")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Company name
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-question").text() shouldBe
        Messages("summaryQuestion.companyName")
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-answer").text() shouldBe
        Messages("common.notAvailable")
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-link")
        .attr("href") shouldEqual routes.IntroductionController.show().toString()
      //Taxpayer Reference
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-question").text() shouldBe
        Messages("summaryQuestion.utr")
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-answer").text() shouldBe
        taxpayerReferenceModel.utr
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-link")
        .attr("href") shouldEqual routes.TaxpayerReferenceController.show().toString()
      //Registered address
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-question").text() shouldBe
        Messages("summaryQuestion.registeredAddress")
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-answer").text() shouldBe
        registeredAddressModel.postcode
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-link")
        .attr("href") shouldEqual routes.RegisteredAddressController.show().toString()
      //Date of incorporation
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(dateOfIncorporationModel.day.get,dateOfIncorporationModel.month.get,dateOfIncorporationModel.year.get)
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual routes.DateOfIncorporationController.show().toString()
      //Nature of business
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-answer").text() shouldBe
        natureOfBusinessModel.natureofbusiness
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual routes.NatureOfBusinessController.show().toString()
      //Has had commercial sale
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-question").text() shouldBe
        Messages("summaryQuestion.hasCommercialSale")
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-answer").text() shouldBe
        commercialSaleModelNo.hasCommercialSale
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-link")
        .attr("href") shouldEqual routes.CommercialSaleController.show().toString()
      //Is Knowledge Intensive
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("knowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.knowledgeIntensive")
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("knowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelNo.isKnowledgeIntensive
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("knowledgeIntensive-link")
        .attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show().toString()
      //Subsidiaries
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("subsidiaries-question").text() shouldBe
        Messages("summaryQuestion.subsidiaries")
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("subsidiaries-answer").text() shouldBe
        subsidiariesModel.ownSubsidiaries
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("subsidiaries-link")
        .attr("href") shouldEqual routes.SubsidiariesController.show().toString()

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SupportingDocumentsController.show().toString()
    }
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models but it does not have a ten year plan and" +
      " so should not have a ten year description row" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(yourCompanyNeedModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(taxpayerReferenceModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(registeredAddressModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(dateOfIncorporationModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(natureOfBusinessModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(commercialSaleModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(isKnowledgeIntensiveModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(operatingCostsModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(percentageStaffWithMastersModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(tenYearPlanModelNo)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(hadPreviousRFIModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(proposedInvestmentModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(whatWillUseForModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(usedInvestmentReasonBeforeModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(previousBeforeDOFCSModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(newGeographicalMarketModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(newProductModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(subsidiariesSpendingInvestmentModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(subsidiariesNinetyOwnedModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(investmentGrowModel)))

        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description")
      document.getElementById("print-this-page").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.print.text")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Company name
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-question").text() shouldBe
        Messages("summaryQuestion.companyName")
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-answer").text() shouldBe
        Messages("common.notAvailable")
      document.getElementById("company-details-table").select("tbody").select("tr").get(0).getElementById("companyName-link")
        .attr("href") shouldEqual routes.IntroductionController.show().toString()
      //Taxpayer Reference
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-question").text() shouldBe
        Messages("summaryQuestion.utr")
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-answer").text() shouldBe
        taxpayerReferenceModel.utr
      document.getElementById("company-details-table").select("tbody").select("tr").get(1).getElementById("utr-link")
        .attr("href") shouldEqual routes.TaxpayerReferenceController.show().toString()
      //Registered address
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-question").text() shouldBe
        Messages("summaryQuestion.registeredAddress")
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-answer").text() shouldBe
        registeredAddressModel.postcode
      document.getElementById("company-details-table").select("tbody").select("tr").get(2).getElementById("registeredAddress-link")
        .attr("href") shouldEqual routes.RegisteredAddressController.show().toString()
      //Date of incorporation
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(dateOfIncorporationModel.day.get,dateOfIncorporationModel.month.get,dateOfIncorporationModel.year.get)
      document.getElementById("company-details-table").select("tbody").select("tr").get(3).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual routes.DateOfIncorporationController.show().toString()
      //Nature of business
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-answer").text() shouldBe
        natureOfBusinessModel.natureofbusiness
      document.getElementById("company-details-table").select("tbody").select("tr").get(4).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual routes.NatureOfBusinessController.show().toString()
      //Has had commercial sale
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-question").text() shouldBe
        Messages("summaryQuestion.hasCommercialSale")
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-answer").text() shouldBe
        commercialSaleModelYes.hasCommercialSale
      document.getElementById("company-details-table").select("tbody").select("tr").get(5).getElementById("hasCommercialSale-link")
        .attr("href") shouldEqual routes.CommercialSaleController.show().toString()
      //Date of first commercial sale
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("commercialSaleDate-question").text() shouldBe
        Messages("summaryQuestion.commercialSaleDate")
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("commercialSaleDate-answer").text() shouldBe
        CommercialSaleModel.toDateString(commercialSaleModelYes.commercialSaleDay.get,commercialSaleModelYes.commercialSaleMonth.get,commercialSaleModelYes.commercialSaleYear.get)
      document.getElementById("company-details-table").select("tbody").select("tr").get(6).getElementById("commercialSaleDate-link")
        .attr("href") shouldEqual routes.CommercialSaleController.show().toString()
      //Is Knowledge Intensive
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("knowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.knowledgeIntensive")
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("knowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      document.getElementById("company-details-table").select("tbody").select("tr").get(7).getElementById("knowledgeIntensive-link")
        .attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show().toString()
      //Operating costs
      document.getElementById("company-details-table").select("tbody").select("tr").get(8).getElementById("operatingCosts-question").text() shouldBe
        Messages("summaryQuestion.operatingCosts")
      document.getElementById("company-details-table").select("tbody").select("tr").get(8).getElementById("operatingCosts-answer").text() should contain
      OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))
      document.getElementById("company-details-table").select("tbody").select("tr").get(8).getElementById("operatingCosts-link")
        .attr("href") shouldEqual routes.OperatingCostsController.show().toString()
      //R&D costs
      document.getElementById("company-details-table").select("tbody").select("tr").get(9).getElementById("rdCosts-question").text() shouldBe
        Messages("summaryQuestion.rdCosts")
      document.getElementById("company-details-table").select("tbody").select("tr").get(9).getElementById("rdCosts-answer").text() should contain
      OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))
      document.getElementById("company-details-table").select("tbody").select("tr").get(9).getElementById("rdCosts-link")
        .attr("href") shouldEqual routes.OperatingCostsController.show().toString()
      //Percentage of staff with masters
      document.getElementById("company-details-table").select("tbody").select("tr").get(10).getElementById("percentageStaffWithMasters-question").text() shouldBe
        Messages("summaryQuestion.percentageStaffWithMasters")
      document.getElementById("company-details-table").select("tbody").select("tr").get(10).getElementById("percentageStaffWithMasters-answer").text() shouldBe
        PercentageStaffWithMastersModel.staffWithMastersToString(percentageStaffWithMastersModel.staffWithMasters)
      document.getElementById("company-details-table").select("tbody").select("tr").get(10).getElementById("percentageStaffWithMasters-link")
        .attr("href") shouldEqual routes.PercentageStaffWithMastersController.show().toString()
      //Has ten year plan
      document.getElementById("company-details-table").select("tbody").select("tr").get(11).getElementById("tenYearPlan-question").text() shouldBe
        Messages("summaryQuestion.tenYearPlan")
      document.getElementById("company-details-table").select("tbody").select("tr").get(11).getElementById("tenYearPlan-answer").text() shouldBe
        tenYearPlanModelNo.hasTenYearPlan
      document.getElementById("company-details-table").select("tbody").select("tr").get(11).getElementById("tenYearPlan-link")
        .attr("href") shouldEqual routes.TenYearPlanController.show().toString()
      //Subsidiaries
      document.getElementById("company-details-table").select("tbody").select("tr").get(12).getElementById("subsidiaries-question").text() shouldBe
        Messages("summaryQuestion.subsidiaries")
      document.getElementById("company-details-table").select("tbody").select("tr").get(12).getElementById("subsidiaries-answer").text() shouldBe
        subsidiariesModel.ownSubsidiaries
      document.getElementById("company-details-table").select("tbody").select("tr").get(12).getElementById("subsidiaries-link")
        .attr("href") shouldEqual routes.SubsidiariesController.show().toString()

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SupportingDocumentsController.show().toString()
    }
  }
}
