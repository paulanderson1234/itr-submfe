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
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.eis.PreviousSchemeController
import controllers.routes
import models.PreviousSchemeModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future

class PreviousSchemeSpec extends ViewSpec {

  object TestController extends PreviousSchemeController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(previousSchemeVectorList: Option[Vector[PreviousSchemeModel]] = None, backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]]
      (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(previousSchemeVectorList))
    when(mockS4lConnector.fetchAndGetFormData[String]
      (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backLink))
  }

  "The Previous Scheme page" should {

    "Verify that the page contains the correct elements for a new scheme model and back link" in new Setup {
      val document: Document = {
        setupMocks(Some(previousSchemeVectorList),Some(controllers.eis.routes.ReviewPreviousSchemesController.show().url))
        val result = TestController.show(None).apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.PreviousScheme.title")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.ReviewPreviousSchemesController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.two")

      document.getElementById("main-heading").text() shouldBe Messages("page.investment.PreviousScheme.heading")

      document.getElementById("guideline").text() shouldBe Messages("page.investment.PreviousScheme.oneAtATime")
      document.getElementById("scheme-type-legend").text() shouldBe Messages("page.investment.PreviousScheme.schemeType")
      document.getElementById("schemeTypeDesc-eisLabel").text() shouldBe Messages("page.previousInvestment.schemeType.eis")
      document.getElementById("schemeTypeDesc-seisLabel").text() shouldBe Messages("page.previousInvestment.schemeType.seis")
      document.getElementById("schemeTypeDesc-sitrLabel").text() shouldBe Messages("page.previousInvestment.schemeType.sitr")
      document.getElementById("schemeTypeDesc-vctLabel").text() shouldBe Messages("page.previousInvestment.schemeType.vct")
      document.getElementById("schemeTypeDesc-otherLabel").text() shouldBe Messages("page.previousInvestment.schemeType.other")
      document.getElementById("label-amount").text() shouldBe Messages("page.investment.PreviousScheme.investmentAmount")

      document.getElementById("label-amount-spent").text() shouldBe Messages("page.previousInvestment.amountSpent.label")
      document.getElementById("label-other-scheme").text() shouldBe Messages("page.investment.PreviousScheme.otherSchemeName.label")

      document.getElementById("question-text-id").text() shouldBe Messages("page.previousInvestment.reviewPreviousSchemes.dateOfShareIssue")
      document.body.getElementById("investmentDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("investmentMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("investmentYear").parent.text shouldBe Messages("common.date.fields.year")

      document.getElementById("help").text() shouldBe Messages("page.investment.PreviousScheme.howToFind")
      document.getElementById("date-of-share-issue-where-to-find").text() should include(Messages("page.investment.PreviousScheme.location"))
      document.getElementById("company-house-db").attr("href") shouldBe "https://www.gov.uk/get-information-about-a-company"
      document.body.getElementById("company-house-db").text() shouldEqual getExternalLinkText(Messages("page.investment.PreviousScheme.companiesHouse"))

      // BUTTON SHOULD BE ADD
      document.getElementById("next").text() shouldBe Messages("page.investment.PreviousScheme.button.add")
    }

    "Verify the page contains the correct elements for an exiting scheme model and changed back link" in new Setup {
      val document: Document = {
        setupMocks(Some(previousSchemeVectorList), Some(controllers.eis.routes.HadPreviousRFIController.show().url))
        val result = TestController.show(Some(previousSchemeModel3.processingId.get)).apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.investment.PreviousScheme.title")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.HadPreviousRFIController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.two")

      document.getElementById("main-heading").text() shouldBe Messages("page.investment.PreviousScheme.heading")

      document.getElementById("guideline").text() shouldBe Messages("page.investment.PreviousScheme.oneAtATime")
      document.getElementById("scheme-type-legend").text() shouldBe Messages("page.investment.PreviousScheme.schemeType")
      document.getElementById("schemeTypeDesc-eisLabel").text() shouldBe Messages("page.previousInvestment.schemeType.eis")
      document.getElementById("schemeTypeDesc-seisLabel").text() shouldBe Messages("page.previousInvestment.schemeType.seis")
      document.getElementById("schemeTypeDesc-sitrLabel").text() shouldBe Messages("page.previousInvestment.schemeType.sitr")
      document.getElementById("schemeTypeDesc-vctLabel").text() shouldBe Messages("page.previousInvestment.schemeType.vct")
      document.getElementById("schemeTypeDesc-otherLabel").text() shouldBe Messages("page.previousInvestment.schemeType.other")
      document.getElementById("label-amount").text() shouldBe Messages("page.previousInvestment.reviewPreviousSchemes.investmentAmountRaised")

      document.getElementById("label-amount-spent").text() shouldBe Messages("page.previousInvestment.amountSpent.label")
      document.getElementById("label-other-scheme").text() shouldBe Messages("page.investment.PreviousScheme.otherSchemeName.label")

      document.getElementById("question-text-id").text() shouldBe Messages("page.previousInvestment.reviewPreviousSchemes.dateOfShareIssue")
      document.body.getElementById("investmentDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("investmentMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("investmentYear").parent.text shouldBe Messages("common.date.fields.year")

      document.getElementById("help").text() shouldBe Messages("page.investment.PreviousScheme.howToFind")
      document.getElementById("date-of-share-issue-where-to-find").text() should include(Messages("page.investment.PreviousScheme.location"))
      document.getElementById("company-house-db").attr("href") shouldBe "https://www.gov.uk/get-information-about-a-company"
      document.body.getElementById("company-house-db").text() shouldEqual getExternalLinkText(Messages("page.investment.PreviousScheme.companiesHouse"))

      // BUTTON SHOULD BE UPDATE
      document.getElementById("next").text() shouldBe Messages("page.investment.PreviousScheme.button.update")
    }

    "Verify the previous scheeme page contains the error summary, button text and back link for invalid new submission" in new Setup {
      val document: Document = {
        setupMocks(Some(previousSchemeVectorList), Some(routes.ApplicationHubController.show().url))
        val result = TestController.submit().apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }

      // BUTTON SHOULD BE ADD
      document.getElementById("next").text() shouldBe Messages("page.investment.PreviousScheme.button.add")
      // SHOULD BE ERROR SECTION AS NO Amount posted
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ApplicationHubController.show().url
    }
  }

}
