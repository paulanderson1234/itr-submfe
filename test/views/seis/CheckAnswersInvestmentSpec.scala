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

package views.seis

import models.ProposedInvestmentModel
import models.seis.SEISCheckAnswersModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.CheckAnswersSpec
import play.api.i18n.Messages.Implicits._
import views.html.seis.checkAndSubmit.CheckAnswers

class CheckAnswersInvestmentSpec extends CheckAnswersSpec {

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 3: Investment" +
      " when it is fully populated with investment models" in {
      val model = SEISCheckAnswersModel(None, None, None, None, None, None, Vector(), Some(proposedInvestmentModel),
        Some(subsidiariesSpendingInvestmentModelNo), Some(subsidiariesNinetyOwnedModelNo), None, None, false)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val investmentTableTbody = document.getElementById("investment-table").select("tbody")

      //Section table heading
      document.getElementById("investmentSection-table-heading").text() shouldBe Messages("page.summaryQuestion.companyDetailsSectionThree")

      //proposed investment
      investmentTableTbody.select("tr").get(0).getElementById("proposedInvestment-question").text() shouldBe
        Messages("page.summaryQuestion.proposedInvestment")
      investmentTableTbody.select("tr").get(0).getElementById("proposedInvestment-answer").text() shouldBe
        ProposedInvestmentModel.getAmountAsFormattedString(proposedInvestmentModel.investmentAmount)
      investmentTableTbody.select("tr").get(0).getElementById("proposedInvestment-link")
        .attr("href") shouldEqual controllers.seis.routes.ProposedInvestmentController.show().url
    }

    "Verify that the Check Answers page contains an empty table for Section 3: Investment" +
      " when the investment models are empty" in new SEISSetup {
      val model = SEISCheckAnswersModel(None, None, None, None, None, None, Vector(), None, None, None, None, None, false)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val investmentTableTbody = document.getElementById("investment-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      //Section table heading
      document.getElementById("investmentSection-table-heading").text() shouldBe Messages("page.summaryQuestion.companyDetailsSectionThree")
      investmentTableTbody.select("tr").size() shouldBe 0
    }
  }
}

