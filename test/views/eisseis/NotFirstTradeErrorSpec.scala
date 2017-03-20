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

import auth.{MockAuthConnector, MockConfig}
import controllers.eisseis.{NotFirstTradeErrorController, routes}
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.eisseis.companyDetails.NotFirstTradeError

class NotFirstTradeErrorSpec extends ViewSpec {

  object TestController extends NotFirstTradeErrorController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "The Not First Trade Start Date Error page" should {

    "Verify that Not First Trade Error page shows both eis and vct bullet points when eis and vct are true" in {
      val page = NotFirstTradeError(SchemeTypesModel(eis = true, seis = true, vct = true))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.heading")
      document.body.getElementById("trading-reason").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.trading.reason")
      document.body.getElementById("what-next-heading").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.whatNext.heading")
      document.body.getElementById("continue-text").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.whatNext.continue")

      document.body.getElementById("valid-schemes").children().size() shouldBe 2
      document.body.getElementById("eis").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.whatNext.scheme.eis")
      document.body.getElementById("vct").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.whatNext.scheme.vct")

      document.body.getElementById("incorrect-info").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.incorrect.info") +
        " " + Messages("page.eisseis.companyDetails.notFirstTradeError.link.changeAnswers") + "."
      document.body.getElementById("change-answers").attr("href") shouldEqual controllers.eisseis.routes.IsFirstTradeController.show().url
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsFirstTradeController.show().url
    }

    "Verify that Not First Trade Error page shows only the eis bullet point when eis is true and vct is false" in {
      val page = NotFirstTradeError(SchemeTypesModel(eis = true, seis = true))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.heading")
      document.body.getElementById("trading-reason").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.trading.reason")
      document.body.getElementById("what-next-heading").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.whatNext.heading")
      document.body.getElementById("continue-text").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.whatNext.continue")

      document.body.getElementById("valid-schemes").children().size() shouldBe 1
      document.body.getElementById("eis").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.whatNext.scheme.eis")

      document.body.getElementById("incorrect-info").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.incorrect.info") +
        " " + Messages("page.eisseis.companyDetails.notFirstTradeError.link.changeAnswers") + "."
      document.body.getElementById("change-answers").attr("href") shouldEqual controllers.eisseis.routes.IsFirstTradeController.show().url
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsFirstTradeController.show().url
    }

    "Verify that Not First Trade Error page shows only the vct bullet point when vct is true and eis is false" in {
      val page = NotFirstTradeError(SchemeTypesModel(seis = true, vct = true))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.heading")
      document.body.getElementById("trading-reason").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.trading.reason")
      document.body.getElementById("what-next-heading").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.whatNext.heading")
      document.body.getElementById("continue-text").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.whatNext.continue")

      document.body.getElementById("valid-schemes").children().size() shouldBe 1
      document.body.getElementById("vct").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.whatNext.scheme.vct")

      document.body.getElementById("incorrect-info").text() shouldEqual Messages("page.eisseis.companyDetails.notFirstTradeError.incorrect.info") +
        " " + Messages("page.eisseis.companyDetails.notFirstTradeError.link.changeAnswers") + "."
      document.body.getElementById("change-answers").attr("href") shouldEqual controllers.eisseis.routes.IsFirstTradeController.show().url
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsFirstTradeController.show().url
    }
  }
}
