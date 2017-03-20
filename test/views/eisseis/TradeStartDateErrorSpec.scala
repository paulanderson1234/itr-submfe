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

package views.eisseis

import auth.{MockConfig, MockAuthConnector}
import controllers.eisseis.{TradeStartDateErrorController, routes}
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import views.html.eisseis.companyDetails.TradeStartDateError

class TradeStartDateErrorSpec extends ViewSpec {

  object TestController extends TradeStartDateErrorController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "The Trade Start Date error page" should {

    "Verify that start page shows both eis and vct bullet points when eis and vct are true" in {
      val page = TradeStartDateError(SchemeTypesModel(eis = true, seis = true, vct = true))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.heading")
      document.body.getElementById("trading-over-two-years").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.trading.over.two.years")
      document.body.getElementById("what-next-heading").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.whatNext.heading")
      document.body.getElementById("continue-text").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.whatNext.continue")


      document.body.getElementById("valid-schemes").children().size() shouldBe 2
      document.body.getElementById("eis").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.whatNext.scheme.eis")
      document.body.getElementById("vct").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.whatNext.scheme.vct")

      document.body.getElementById("incorrect-info").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.incorrect.info") +
        " " + Messages("page.eisseis.companyDetails.tradeStartDateError.link.changeAnswers") + "."
      document.body.getElementById("change-answers").attr("href") shouldEqual controllers.eisseis.routes.TradeStartDateController.show().url
      document.body.getElementById("back-link").attr("href") shouldEqual routes.TradeStartDateController.show().url
    }

    "Verify that start page shows only the eis bullet point when eis is true and vct is false" in {
      val page = TradeStartDateError(SchemeTypesModel(eis = true, seis = true))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.heading")
      document.body.getElementById("trading-over-two-years").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.trading.over.two.years")
      document.body.getElementById("what-next-heading").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.whatNext.heading")
      document.body.getElementById("continue-text").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.whatNext.continue")


      document.body.getElementById("valid-schemes").children().size() shouldBe 1
      document.body.getElementById("eis").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.whatNext.scheme.eis")

      document.body.getElementById("incorrect-info").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.incorrect.info") +
        " " + Messages("page.eisseis.companyDetails.tradeStartDateError.link.changeAnswers") + "."
      document.body.getElementById("change-answers").attr("href") shouldEqual controllers.eisseis.routes.TradeStartDateController.show().url
      document.body.getElementById("back-link").attr("href") shouldEqual routes.TradeStartDateController.show().url
    }

    "Verify that start page shows only the vct bullet point when vct is true and eis is false" in {
      val page = TradeStartDateError(SchemeTypesModel(seis = true, vct = true))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.heading")
      document.body.getElementById("trading-over-two-years").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.trading.over.two.years")
      document.body.getElementById("what-next-heading").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.whatNext.heading")
      document.body.getElementById("continue-text").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.whatNext.continue")


      document.body.getElementById("valid-schemes").children().size() shouldBe 1
      document.body.getElementById("vct").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.whatNext.scheme.vct")

      document.body.getElementById("incorrect-info").text() shouldEqual Messages("page.eisseis.companyDetails.tradeStartDateError.incorrect.info") +
        " " + Messages("page.eisseis.companyDetails.tradeStartDateError.link.changeAnswers") + "."
      document.body.getElementById("change-answers").attr("href") shouldEqual controllers.eisseis.routes.TradeStartDateController.show().url
      document.body.getElementById("back-link").attr("href") shouldEqual routes.TradeStartDateController.show().url
    }
  }
}
