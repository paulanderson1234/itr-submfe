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

import auth.{Enrolment, Identifier, MockAuthConnector}
import common.{Constants, KeystoreKeys}
import config.FrontendAppConfig
import connectors.{EnrolmentConnector, S4LConnector}
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

class CheckAnswersInvestmentSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach {

  val mockS4lConnector = mock[S4LConnector]

  // Data for investment section tests
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

  class SetupPage {

    val controller = new CheckAnswersController {
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val s4lConnector: S4LConnector = mockS4lConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  override def beforeEach() {
    reset(mockS4lConnector)
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 3: Investment" +
      " when it is fully populated with investment models" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(proposedInvestmentModel)))
        when(mockS4lConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(whatWillUseForModel)))
        when(mockS4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(usedInvestmentReasonBeforeModel)))
        when(mockS4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(previousBeforeDOFCSModel)))
        when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(newGeographicalMarketModel)))
        when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(newProductModel)))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(subsidiariesSpendingInvestmentModel)))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(Option(subsidiariesNinetyOwnedModel)))
        when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(investmentGrowModel)))


        // other sections not being tested
        when(mockS4lConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))


        val result = controller.show.apply(authorisedFakeRequest.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }


      lazy val investmentTableTbody = document.getElementById("investment-table").select("tbody")

      //Section table heading
      document.getElementById("investmentSection-table-heading").text() shouldBe Messages("page.summaryQuestion.companyDetailsSectionThree")

      //proposed investment
      investmentTableTbody.select("tr").get(0).getElementById("proposedInvestment-question").text() shouldBe
        Messages("page.summaryQuestion.proposedInvestment")
      investmentTableTbody.select("tr").get(0).getElementById("proposedInvestment-answer").text() shouldBe
        ProposedInvestmentModel.getAmountAsFormattedString(proposedInvestmentModel.investmentAmount)
      investmentTableTbody.select("tr").get(0).getElementById("proposedInvestment-link")
        .attr("href") shouldEqual routes.ProposedInvestmentController.show().toString

      //what use investment for
      investmentTableTbody.select("tr").get(1).getElementById("whatWillUseFor-question").text() shouldBe
        Messages("page.summaryQuestion.whatWillUseFor")
      investmentTableTbody.select("tr").get(1).getElementById("whatWillUseFor-answer").text() shouldBe
        WhatWillUseForModel.purposeTransformation(whatWillUseForModel.whatWillUseFor)
      investmentTableTbody.select("tr").get(1).getElementById("whatWillUseFor-link")
        .attr("href") shouldBe routes.WhatWillUseForController.show().toString

      // same reason as before
      investmentTableTbody.select("tr").get(2).getElementById("usedInvestReasonBefore-question").text() shouldBe
        Messages("page.summaryQuestion.usedInvestReasonBefore")
      investmentTableTbody.select("tr").get(2).getElementById("usedInvestReasonBefore-answer").text() shouldBe
        usedInvestmentReasonBeforeModel.usedInvestmentReasonBefore
      investmentTableTbody.select("tr").get(2).getElementById("usedInvestReasonBefore-link")
        .attr("href") shouldEqual routes.UsedInvestmentReasonBeforeController.show().toString

      // previous docfs
      investmentTableTbody.select("tr").get(3).getElementById("previousBeforeDOFCS-question").text() shouldBe
        Messages("page.summaryQuestion.previousBeforeDOFCS")
      investmentTableTbody.select("tr").get(3).getElementById("previousBeforeDOFCS-answer").text() shouldBe
        previousBeforeDOFCSModel.previousBeforeDOFCS
      investmentTableTbody.select("tr").get(3).getElementById("previousBeforeDOFCS-link")
        .attr("href") shouldEqual routes.PreviousBeforeDOFCSController.show().toString

      // new geographical market
      investmentTableTbody.select("tr").get(4).getElementById("newGeoMarket-question").text() shouldBe
        Messages("page.summaryQuestion.newGeoMarket")
      investmentTableTbody.select("tr").get(4).getElementById("newGeoMarket-answer").text() shouldBe
        newGeographicalMarketModel.isNewGeographicalMarket
      investmentTableTbody.select("tr").get(4).getElementById("newGeoMarket-link")
        .attr("href") shouldEqual routes.NewGeographicalMarketController.show().toString

      // new prodcut
      investmentTableTbody.select("tr").get(5).getElementById("newProduct-question").text() shouldBe
        Messages("page.summaryQuestion.newProduct")
      investmentTableTbody.select("tr").get(5).getElementById("newProduct-answer").text() shouldBe
        newGeographicalMarketModel.isNewGeographicalMarket
      investmentTableTbody.select("tr").get(5).getElementById("newProduct-link")
        .attr("href") shouldEqual routes.NewProductController.show().toString

      // subsidiaries spending investment
      investmentTableTbody.select("tr").get(6).getElementById("subsSpendingInvest-question").text() shouldBe
        Messages("page.summaryQuestion.subsSpendingInvest")
      investmentTableTbody.select("tr").get(6).getElementById("subsSpendingInvest-answer").text() shouldBe
        subsidiariesSpendingInvestmentModel.subSpendingInvestment
      investmentTableTbody.select("tr").get(6).getElementById("subsSpendingInvest-link")
        .attr("href") shouldEqual routes.SubsidiariesSpendingInvestmentController.show().toString

      // 90% owned
      investmentTableTbody.select("tr").get(7).getElementById("subNinetyOwned-question").text() shouldBe
        Messages("page.summaryQuestion.subNinetyOwned")
      investmentTableTbody.select("tr").get(7).getElementById("subNinetyOwned-answer").text() shouldBe
        subsidiariesNinetyOwnedModel.ownNinetyPercent
      investmentTableTbody.select("tr").get(7).getElementById("subNinetyOwned-link")
        .attr("href") shouldEqual routes.SubsidiariesNinetyOwnedController.show().toString

      // investment Grow
      investmentTableTbody.select("tr").get(8).getElementById("investmentGrow-question").text() shouldBe
        Messages("page.summaryQuestion.investmentGrow")
      investmentTableTbody.select("tr").get(8).getElementById("investmentGrow-answer").text() shouldBe
        investmentGrowModel.investmentGrowDesc
      investmentTableTbody.select("tr").get(8).getElementById("investmentGrow-link")
        .attr("href") shouldEqual routes.InvestmentGrowController.show().toString

    }
  }


  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 3: Investment" +
      " when the investment models are empty" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))

        when(mockS4lConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))


        val result = controller.show.apply(authorisedFakeRequest.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }


      lazy val investmentTableTbody = document.getElementById("investment-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      //Section table heading
      document.getElementById("investmentSection-table-heading").text() shouldBe Messages("page.summaryQuestion.companyDetailsSectionThree")

      //proposed investment
      investmentTableTbody.select("tr").get(0).getElementById("proposedInvestment-question").text() shouldBe
        Messages("page.summaryQuestion.proposedInvestment")
      investmentTableTbody.select("tr").get(0).getElementById("proposedInvestment-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(0).getElementById("proposedInvestment-link")
        .attr("href") shouldEqual routes.ProposedInvestmentController.show().toString

      //what use investment for
      investmentTableTbody.select("tr").get(1).getElementById("whatWillUseFor-question").text() shouldBe
        Messages("page.summaryQuestion.whatWillUseFor")
      investmentTableTbody.select("tr").get(1).getElementById("whatWillUseFor-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(1).getElementById("whatWillUseFor-link")
        .attr("href") shouldBe routes.WhatWillUseForController.show().toString

      // same reason as before
      investmentTableTbody.select("tr").get(2).getElementById("usedInvestReasonBefore-question").text() shouldBe
        Messages("page.summaryQuestion.usedInvestReasonBefore")
      investmentTableTbody.select("tr").get(2).getElementById("usedInvestReasonBefore-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(2).getElementById("usedInvestReasonBefore-link")
        .attr("href") shouldEqual routes.UsedInvestmentReasonBeforeController.show().toString

      // previous docfs
      investmentTableTbody.select("tr").get(3).getElementById("previousBeforeDOFCS-question").text() shouldBe
        Messages("page.summaryQuestion.previousBeforeDOFCS")
      investmentTableTbody.select("tr").get(3).getElementById("previousBeforeDOFCS-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(3).getElementById("previousBeforeDOFCS-link")
        .attr("href") shouldEqual routes.PreviousBeforeDOFCSController.show().toString

      // new geographical market
      investmentTableTbody.select("tr").get(4).getElementById("newGeoMarket-question").text() shouldBe
        Messages("page.summaryQuestion.newGeoMarket")
      investmentTableTbody.select("tr").get(4).getElementById("newGeoMarket-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(4).getElementById("newGeoMarket-link")
        .attr("href") shouldEqual routes.NewGeographicalMarketController.show().toString

      // new prodcut
      investmentTableTbody.select("tr").get(5).getElementById("newProduct-question").text() shouldBe
        Messages("page.summaryQuestion.newProduct")
      investmentTableTbody.select("tr").get(5).getElementById("newProduct-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(5).getElementById("newProduct-link")
        .attr("href") shouldEqual routes.NewProductController.show().toString

      // subsidiaries spending investment
      investmentTableTbody.select("tr").get(6).getElementById("subsSpendingInvest-question").text() shouldBe
        Messages("page.summaryQuestion.subsSpendingInvest")
      investmentTableTbody.select("tr").get(6).getElementById("subsSpendingInvest-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(6).getElementById("subsSpendingInvest-link")
        .attr("href") shouldEqual routes.SubsidiariesSpendingInvestmentController.show().toString

      // 90% owned
      investmentTableTbody.select("tr").get(7).getElementById("subNinetyOwned-question").text() shouldBe
        Messages("page.summaryQuestion.subNinetyOwned")
      investmentTableTbody.select("tr").get(7).getElementById("subNinetyOwned-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(7).getElementById("subNinetyOwned-link")
        .attr("href") shouldEqual routes.SubsidiariesNinetyOwnedController.show().toString

      // investment Grow
      investmentTableTbody.select("tr").get(8).getElementById("investmentGrow-question").text() shouldBe
        Messages("page.summaryQuestion.investmentGrow")
      investmentTableTbody.select("tr").get(8).getElementById("investmentGrow-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(8).getElementById("investmentGrow-link")
        .attr("href") shouldEqual routes.InvestmentGrowController.show().toString

    }
  }
}
