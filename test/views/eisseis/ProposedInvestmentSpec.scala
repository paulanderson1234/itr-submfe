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
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.eisseis.ProposedInvestmentController
import controllers.routes
import models.ProposedInvestmentModel
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class ProposedInvestmentSpec extends ViewSpec {
  
  object TestController extends ProposedInvestmentController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }
  
  def setupMocks(proposedInvestmentModel: Option[ProposedInvestmentModel] = None, backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel]
      (Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(proposedInvestmentModel))
    when(mockS4lConnector.fetchAndGetFormData[String]
      (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisSchemeTypesModel))
  }
  
  "The Proposed Investment page" should {

    "Verify that the proposed investment page contains the correct elements when a valid ProposedInvestmentModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(proposedInvestmentModel),Some(controllers.eisseis.routes.TenYearPlanController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount-hint").text() shouldBe Messages("page.investment.amount.hint")
      document.getElementById("help").text() shouldBe Messages("page.investment.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.investment.help.link.text")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.TenYearPlanController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    }

    "Verify that the proposed investment page contains the correct elements when an invalid ProposedInvestmentModel is passed" in new Setup {
      val document: Document = {
        setupMocks(backLink = Some(controllers.eisseis.routes.HadPreviousRFIController.show().url))
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount-hint").text() shouldBe Messages("page.investment.amount.hint")
      document.getElementById("help").text() shouldBe Messages("page.investment.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.investment.help.link.text")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.HadPreviousRFIController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

    "Verify that the proposed investment page contains the correct elements when an None ProposedInvestmentModel is passed" in new Setup {
      val document: Document = {
        setupMocks(backLink = Some(controllers.eisseis.routes.ReviewPreviousSchemesController.show().url))
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount-hint").text() shouldBe Messages("page.investment.amount.hint")
      document.getElementById("help").text() shouldBe Messages("page.investment.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.investment.help.link.text")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ReviewPreviousSchemesController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }
  }

}
