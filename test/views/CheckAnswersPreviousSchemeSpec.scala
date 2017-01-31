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

package views

import auth.MockAuthConnector
import config.FrontendAppConfig
import controllers.{CheckAnswersController, routes}
import controllers.helpers.FakeRequestHelper
import models.PreviousSchemeModel
import models.PreviousSchemeModel._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{UnitSpec}
import views.helpers.CheckAnswersSpec
import play.api.test.Helpers._
import play.api.i18n.Messages.Implicits._

class CheckAnswersPreviousSchemeSpec extends CheckAnswersSpec {

  object TestController extends CheckAnswersController {
     override lazy val applicationConfig = FrontendAppConfig
     override lazy val authConnector = MockAuthConnector
     override lazy val s4lConnector = mockS4lConnector
     override lazy val enrolmentConnector = mockEnrolmentConnector
   }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 2: Previous Schemes" +
      " when a Vector of previous schemes can be retrieved" in new Setup {
      val document: Document = {
        previousRFISetup(Some(hadPreviousRFIModelYes), Some(previousSchemesValid))
        investmentSetup()
        contactDetailsSetup()
        companyDetailsSetup()
        contactAddressSetup()
        val result = TestController.show(None).apply(authorisedFakeRequest.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")


      //Section 1 table heading
      document.getElementById("previousRFISection-table-heading").text() shouldBe Messages("summaryQuestion.previousRFISection")
      //Previous RFI None
      document.getElementById("previousScheme-0-question").text shouldBe PreviousSchemeModel.getSchemeName(previousSchemesValid(0).schemeTypeDesc)
      document.getElementById("previousScheme-0-Line0").text shouldBe
        s"${Messages("page.investment.amount.label")} ${getAmountAsFormattedString(previousSchemesValid(0).investmentAmount)}"
      document.getElementById("previousScheme-0-Line1").text shouldBe
        s"${Messages("page.investment.amountSpent.label")} ${getAmountAsFormattedString(previousSchemesValid(0).investmentSpent.get)}"
      document.getElementById("previousScheme-0-Line2").text shouldBe
        s"${Messages("page.investment.dateOfShareIssue.label")} ${toDateString(previousSchemesValid(0).day.get,previousSchemesValid(0).month.get,
          previousSchemesValid(0).year.get)}"
      document.getElementById("previousScheme-0-link").attr("href") shouldBe routes.ReviewPreviousSchemesController.show().url
      document.getElementById("previousScheme-1-question").text shouldBe PreviousSchemeModel.getSchemeName(previousSchemesValid(1).schemeTypeDesc)
      document.getElementById("previousScheme-1-Line0").text shouldBe
        s"${Messages("page.investment.amount.label")} ${getAmountAsFormattedString(previousSchemesValid(1).investmentAmount)}"
      document.getElementById("previousScheme-1-Line1").text shouldBe
        s"${Messages("page.investment.amountSpent.label")} ${getAmountAsFormattedString(previousSchemesValid(1).investmentSpent.get)}"
      document.getElementById("previousScheme-1-Line2").text shouldBe
        s"${Messages("page.investment.dateOfShareIssue.label")} ${toDateString(previousSchemesValid(1).day.get,previousSchemesValid(1).month.get,
          previousSchemesValid(1).year.get)}"
      document.getElementById("previousScheme-1-link").attr("href") shouldBe routes.ReviewPreviousSchemesController.show().url
      document.getElementById("previousScheme-2-question").text shouldBe previousSchemesValid(2).otherSchemeName.get
      document.getElementById("previousScheme-2-Line0").text shouldBe
        s"${Messages("page.investment.amount.label")} ${getAmountAsFormattedString(previousSchemesValid(2).investmentAmount)}"
      document.getElementById("previousScheme-2-Line1").text shouldBe
        s"${Messages("page.investment.amountSpent.label")} ${getAmountAsFormattedString(previousSchemesValid(2).investmentSpent.get)}"
      document.getElementById("previousScheme-2-Line2").text shouldBe
        s"${Messages("page.investment.dateOfShareIssue.label")} ${toDateString(previousSchemesValid(2).day.get,previousSchemesValid(2).month.get,
          previousSchemesValid(2).year.get)}"
      document.getElementById("previousScheme-2-link").attr("href") shouldBe routes.ReviewPreviousSchemesController.show().url
      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SupportingDocumentsController.show().url
    }

    "Verify that the Check Answers page contains the correct elements for Section 2: Previous Schemes" +
      " when an empty Vector is be retrieved" in new Setup {
      val document: Document = {
        previousRFISetup()
        investmentSetup()
        contactDetailsSetup()
        companyDetailsSetup()
        contactAddressSetup()
        val result = TestController.show(None).apply(authorisedFakeRequest.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")


      //Section 1 table heading
      document.getElementById("previousRFISection-table-heading").text() shouldBe Messages("summaryQuestion.previousRFISection")
      //Previous RFI None
      document.getElementById("noPreviousScheme-question").text shouldBe Messages("page.summaryQuestion.none.question")
      document.getElementById("noPreviousScheme-answer").text shouldBe Messages("page.summaryQuestion.none.answer")
      document.getElementById("noPreviousScheme-link").attr("href") shouldBe routes.HadPreviousRFIController.show().url
      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SupportingDocumentsController.show().url
    }
  }
}
