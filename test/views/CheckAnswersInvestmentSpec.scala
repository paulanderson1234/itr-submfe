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

package views

import auth.MockAuthConnector
import config.FrontendAppConfig
import controllers.{CheckAnswersController, routes}
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.helpers.CheckAnswersHelper
import play.api.test.Helpers._

class CheckAnswersInvestmentSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with BeforeAndAfterEach with CheckAnswersHelper {
  
  object TestController extends CheckAnswersController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  override def beforeEach() {
    reset(mockS4lConnector)
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 3: Investment" +
      " when it is fully populated with investment models" in new Setup {
      val document: Document = {
        previousSchemeSetup()
        investmentSetup(Some(proposedInvestmentModel),Some(whatWillUseForModel),Some(usedInvestmentReasonBeforeModel),
        Some(previousBeforeDOFCSModel),Some(newGeographicalMarketModel),Some(newProductModel),Some(subsidiariesSpendingInvestmentModel),
        Some(subsidiariesNinetyOwnedModel),Some(investmentGrowModel))
        contactDetailsSetup()
        companyDetailsSetup()
        val result = TestController.show.apply(authorisedFakeRequest.withFormUrlEncodedBody())
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
        .attr("href") shouldEqual routes.ProposedInvestmentController.show().url

      //what use investment for
      investmentTableTbody.select("tr").get(1).getElementById("whatWillUseFor-question").text() shouldBe
        Messages("page.summaryQuestion.whatWillUseFor")
      investmentTableTbody.select("tr").get(1).getElementById("whatWillUseFor-answer").text() shouldBe
        WhatWillUseForModel.purposeTransformation(whatWillUseForModel.whatWillUseFor)
      investmentTableTbody.select("tr").get(1).getElementById("whatWillUseFor-link")
        .attr("href") shouldBe routes.WhatWillUseForController.show().url

      // same reason as before
      investmentTableTbody.select("tr").get(2).getElementById("usedInvestReasonBefore-question").text() shouldBe
        Messages("page.summaryQuestion.usedInvestReasonBefore")
      investmentTableTbody.select("tr").get(2).getElementById("usedInvestReasonBefore-answer").text() shouldBe
        usedInvestmentReasonBeforeModel.usedInvestmentReasonBefore
      investmentTableTbody.select("tr").get(2).getElementById("usedInvestReasonBefore-link")
        .attr("href") shouldEqual routes.UsedInvestmentReasonBeforeController.show().url

      // previous docfs
      investmentTableTbody.select("tr").get(3).getElementById("previousBeforeDOFCS-question").text() shouldBe
        Messages("page.summaryQuestion.previousBeforeDOFCS")
      investmentTableTbody.select("tr").get(3).getElementById("previousBeforeDOFCS-answer").text() shouldBe
        previousBeforeDOFCSModel.previousBeforeDOFCS
      investmentTableTbody.select("tr").get(3).getElementById("previousBeforeDOFCS-link")
        .attr("href") shouldEqual routes.PreviousBeforeDOFCSController.show().url

      // new geographical market
      investmentTableTbody.select("tr").get(4).getElementById("newGeoMarket-question").text() shouldBe
        Messages("page.summaryQuestion.newGeoMarket")
      investmentTableTbody.select("tr").get(4).getElementById("newGeoMarket-answer").text() shouldBe
        newGeographicalMarketModel.isNewGeographicalMarket
      investmentTableTbody.select("tr").get(4).getElementById("newGeoMarket-link")
        .attr("href") shouldEqual routes.NewGeographicalMarketController.show().url

      // new prodcut
      investmentTableTbody.select("tr").get(5).getElementById("newProduct-question").text() shouldBe
        Messages("page.summaryQuestion.newProduct")
      investmentTableTbody.select("tr").get(5).getElementById("newProduct-answer").text() shouldBe
        newGeographicalMarketModel.isNewGeographicalMarket
      investmentTableTbody.select("tr").get(5).getElementById("newProduct-link")
        .attr("href") shouldEqual routes.NewProductController.show().url

      // subsidiaries spending investment
      investmentTableTbody.select("tr").get(6).getElementById("subsSpendingInvest-question").text() shouldBe
        Messages("page.summaryQuestion.subsSpendingInvest")
      investmentTableTbody.select("tr").get(6).getElementById("subsSpendingInvest-answer").text() shouldBe
        subsidiariesSpendingInvestmentModel.subSpendingInvestment
      investmentTableTbody.select("tr").get(6).getElementById("subsSpendingInvest-link")
        .attr("href") shouldEqual routes.SubsidiariesSpendingInvestmentController.show().url

      // 90% owned
      investmentTableTbody.select("tr").get(7).getElementById("subNinetyOwned-question").text() shouldBe
        Messages("page.summaryQuestion.subNinetyOwned")
      investmentTableTbody.select("tr").get(7).getElementById("subNinetyOwned-answer").text() shouldBe
        subsidiariesNinetyOwnedModel.ownNinetyPercent
      investmentTableTbody.select("tr").get(7).getElementById("subNinetyOwned-link")
        .attr("href") shouldEqual routes.SubsidiariesNinetyOwnedController.show().url

      // investment Grow
      investmentTableTbody.select("tr").get(8).getElementById("investmentGrow-question").text() shouldBe
        Messages("page.summaryQuestion.investmentGrow")
      investmentTableTbody.select("tr").get(8).getElementById("investmentGrow-answer").text() shouldBe
        investmentGrowModel.investmentGrowDesc
      investmentTableTbody.select("tr").get(8).getElementById("investmentGrow-link")
        .attr("href") shouldEqual routes.InvestmentGrowController.show().url

    }
  }


  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 3: Investment" +
      " when the investment models are empty" in new Setup {
      val document: Document = {
        previousSchemeSetup()
        investmentSetup()
        contactDetailsSetup()
        companyDetailsSetup()
        val result = TestController.show.apply(authorisedFakeRequest.withFormUrlEncodedBody())
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
        .attr("href") shouldEqual routes.ProposedInvestmentController.show().url

      //what use investment for
      investmentTableTbody.select("tr").get(1).getElementById("whatWillUseFor-question").text() shouldBe
        Messages("page.summaryQuestion.whatWillUseFor")
      investmentTableTbody.select("tr").get(1).getElementById("whatWillUseFor-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(1).getElementById("whatWillUseFor-link")
        .attr("href") shouldBe routes.WhatWillUseForController.show().url

      // same reason as before
      investmentTableTbody.select("tr").get(2).getElementById("usedInvestReasonBefore-question").text() shouldBe
        Messages("page.summaryQuestion.usedInvestReasonBefore")
      investmentTableTbody.select("tr").get(2).getElementById("usedInvestReasonBefore-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(2).getElementById("usedInvestReasonBefore-link")
        .attr("href") shouldEqual routes.UsedInvestmentReasonBeforeController.show().url

      // previous docfs
      investmentTableTbody.select("tr").get(3).getElementById("previousBeforeDOFCS-question").text() shouldBe
        Messages("page.summaryQuestion.previousBeforeDOFCS")
      investmentTableTbody.select("tr").get(3).getElementById("previousBeforeDOFCS-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(3).getElementById("previousBeforeDOFCS-link")
        .attr("href") shouldEqual routes.PreviousBeforeDOFCSController.show().url

      // new geographical market
      investmentTableTbody.select("tr").get(4).getElementById("newGeoMarket-question").text() shouldBe
        Messages("page.summaryQuestion.newGeoMarket")
      investmentTableTbody.select("tr").get(4).getElementById("newGeoMarket-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(4).getElementById("newGeoMarket-link")
        .attr("href") shouldEqual routes.NewGeographicalMarketController.show().url

      // new prodcut
      investmentTableTbody.select("tr").get(5).getElementById("newProduct-question").text() shouldBe
        Messages("page.summaryQuestion.newProduct")
      investmentTableTbody.select("tr").get(5).getElementById("newProduct-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(5).getElementById("newProduct-link")
        .attr("href") shouldEqual routes.NewProductController.show().url

      // subsidiaries spending investment
      investmentTableTbody.select("tr").get(6).getElementById("subsSpendingInvest-question").text() shouldBe
        Messages("page.summaryQuestion.subsSpendingInvest")
      investmentTableTbody.select("tr").get(6).getElementById("subsSpendingInvest-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(6).getElementById("subsSpendingInvest-link")
        .attr("href") shouldEqual routes.SubsidiariesSpendingInvestmentController.show().url

      // 90% owned
      investmentTableTbody.select("tr").get(7).getElementById("subNinetyOwned-question").text() shouldBe
        Messages("page.summaryQuestion.subNinetyOwned")
      investmentTableTbody.select("tr").get(7).getElementById("subNinetyOwned-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(7).getElementById("subNinetyOwned-link")
        .attr("href") shouldEqual routes.SubsidiariesNinetyOwnedController.show().url

      // investment Grow
      investmentTableTbody.select("tr").get(8).getElementById("investmentGrow-question").text() shouldBe
        Messages("page.summaryQuestion.investmentGrow")
      investmentTableTbody.select("tr").get(8).getElementById("investmentGrow-answer").text() shouldBe notAvailableMessage
      investmentTableTbody.select("tr").get(8).getElementById("investmentGrow-link")
        .attr("href") shouldEqual routes.InvestmentGrowController.show().url

    }
  }
}
