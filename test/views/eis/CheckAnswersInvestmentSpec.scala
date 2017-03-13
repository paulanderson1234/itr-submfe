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

package views.eis

import auth.{MockConfigEISFlow, MockAuthConnector}
import config.FrontendAppConfig
import controllers.eis.CheckAnswersController
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.CheckAnswersSpec

class CheckAnswersInvestmentSpec extends CheckAnswersSpec {
  
  object TestController extends CheckAnswersController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 3: Investment" +
      " when it is fully populated with investment models" in new Setup {
      val document: Document = {
        previousRFISetup()
        investmentSetup(Some(proposedInvestmentModel),Some(usedInvestmentReasonBeforeModelYes),
        Some(previousBeforeDOFCSModelYes),Some(newGeographicalMarketModelYes),Some(newProductMarketModelYes),Some(subsidiariesSpendingInvestmentModelYes),
        Some(subsidiariesNinetyOwnedModelNo),Some(investmentGrowModel))
        contactDetailsSetup()
        companyDetailsSetup()
        contactAddressSetup()
        val result = TestController.show(None).apply(authorisedFakeRequest.withFormUrlEncodedBody())
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
        .attr("href") shouldEqual controllers.eis.routes.ProposedInvestmentController.show().url


      // same reason as before
      investmentTableTbody.select("tr").get(1).getElementById("usedInvestReasonBefore-question").text() shouldBe
        Messages("page.summaryQuestion.usedInvestReasonBefore")
      investmentTableTbody.select("tr").get(1).getElementById("usedInvestReasonBefore-answer").text() shouldBe
        usedInvestmentReasonBeforeModelYes.usedInvestmentReasonBefore
      investmentTableTbody.select("tr").get(1).getElementById("usedInvestReasonBefore-link")
        .attr("href") shouldEqual controllers.eis.routes.UsedInvestmentReasonBeforeController.show().url

      // previous docfs
      investmentTableTbody.select("tr").get(2).getElementById("previousBeforeDOFCS-question").text() shouldBe
        Messages("page.summaryQuestion.previousBeforeDOFCS")
      investmentTableTbody.select("tr").get(2).getElementById("previousBeforeDOFCS-answer").text() shouldBe
        previousBeforeDOFCSModelYes.previousBeforeDOFCS
      investmentTableTbody.select("tr").get(2).getElementById("previousBeforeDOFCS-link")
        .attr("href") shouldEqual controllers.eis.routes.PreviousBeforeDOFCSController.show().url

      // new geographical market
      investmentTableTbody.select("tr").get(3).getElementById("newGeoMarket-question").text() shouldBe
        Messages("page.summaryQuestion.newGeoMarket")
      investmentTableTbody.select("tr").get(3).getElementById("newGeoMarket-answer").text() shouldBe
        newGeographicalMarketModelYes.isNewGeographicalMarket
      investmentTableTbody.select("tr").get(3).getElementById("newGeoMarket-link")
        .attr("href") shouldEqual controllers.eis.routes.NewGeographicalMarketController.show().url

      // new prodcut
      investmentTableTbody.select("tr").get(4).getElementById("newProduct-question").text() shouldBe
        Messages("page.summaryQuestion.newProduct")
      investmentTableTbody.select("tr").get(4).getElementById("newProduct-answer").text() shouldBe
        newGeographicalMarketModelYes.isNewGeographicalMarket
      investmentTableTbody.select("tr").get(4).getElementById("newProduct-link")
        .attr("href") shouldEqual controllers.eis.routes.NewProductController.show().url

      // investment Grow
      investmentTableTbody.select("tr").get(5).getElementById("investmentGrow-question").text() shouldBe
        Messages("page.summaryQuestion.investmentGrow")
      investmentTableTbody.select("tr").get(5).getElementById("investmentGrow-answer").text() shouldBe
        investmentGrowModel.investmentGrowDesc
      investmentTableTbody.select("tr").get(5).getElementById("investmentGrow-link")
        .attr("href") shouldEqual controllers.eis.routes.InvestmentGrowController.show().url
    }
  }


  "The Check Answers page" should {

    "Verify that the Check Answers page contains an empty table for Section 3: Investment" +
      " when the investment models are empty" in new Setup {
      val document: Document = {
        previousRFISetup()
        investmentSetup()
        contactDetailsSetup()
        companyDetailsSetup()
        contactAddressSetup()
        val result = TestController.show(None).apply(authorisedFakeRequest.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }

      lazy val investmentTableTbody = document.getElementById("investment-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      //Section table heading
      document.getElementById("investmentSection-table-heading").text() shouldBe Messages("page.summaryQuestion.companyDetailsSectionThree")
      investmentTableTbody.select("tr").size() shouldBe 0
    }
  }
}
