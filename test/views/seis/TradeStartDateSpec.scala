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

import auth.{MockConfigSingleFlow, MockAuthConnector}
import common.{Constants, KeystoreKeys}
import connectors.SubmissionConnector
import controllers.seis.TradeStartDateController
import models.TradeStartDateModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future

class TradeStartDateSpec extends ViewSpec {

  val tradeStartDateModelInvalidYes = new TradeStartDateModel(Constants.StandardRadioButtonYesValue, None, Some(25), Some(2015))

  object TestController extends TradeStartDateController {
    override lazy val applicationConfig = MockConfigSingleFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val submissionConnector: SubmissionConnector = mockSubmissionConnector
  }

  def setupMocks(tradeStartDateModel: Option[TradeStartDateModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(tradeStartDateModel))

  "The Trade Start Date page" should {

    "Verify that the Trade start date  page contains the correct elements when a valid 'Yes' TradeStartDateModel is passed" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(tradeStartDateModelYes))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.seis.companyDetails.tradeStartDate.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.seis.companyDetails.tradeStartDate.heading")
      document.getElementById("form-hint-id").text() shouldBe Messages("common.date.hint.example")
      document.getElementById("question-text-id").text() shouldBe Messages("page.seis.companyDetails.TradeStartDate.question.hint")
      document.getElementById("question-text-id").hasClass("h2-heading")
      document.getElementById("question-date-text-legend-id").hasClass("visuallyhidden")
      document.getElementById("hasTradeStartDate-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasTradeStartDate-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.DateOfIncorporationController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
    }


    "Verify that the Trade start date  page contains the correct elements when a valid 'No' TradeStartDateModel is passed" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(tradeStartDateModelNo))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.seis.companyDetails.tradeStartDate.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.seis.companyDetails.tradeStartDate.heading")
      document.getElementById("form-hint-id").text() shouldBe Messages("common.date.hint.example")
      document.getElementById("question-text-id").text() shouldBe Messages("page.seis.companyDetails.TradeStartDate.question.hint")
      document.getElementById("question-text-id").hasClass("h2-heading")
      document.getElementById("question-date-text-legend-id").hasClass("visuallyhidden")
      document.getElementById("hasTradeStartDate-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasTradeStartDate-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.DateOfIncorporationController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
    }

    "Verify that the Trade start date  page contains the correct elements when an invalid TradeStartDateModel is passed" in new SEISSetup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.seis.companyDetails.tradeStartDate.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.seis.companyDetails.tradeStartDate.heading")
      document.getElementById("form-hint-id").text() shouldBe Messages("common.date.hint.example")
      document.getElementById("question-text-id").text() shouldBe Messages("page.seis.companyDetails.TradeStartDate.question.hint")
      document.getElementById("question-text-id").hasClass("h2-heading")
      document.getElementById("question-date-text-legend-id").hasClass("visuallyhidden")
      document.getElementById("hasTradeStartDate-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasTradeStartDate-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.DateOfIncorporationController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

    "Verify that the Trade start date  page contains the correct elements when an invalid TradeStartDateYesModel is passed" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(tradeStartDateModelInvalidYes))
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.seis.companyDetails.tradeStartDate.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.seis.companyDetails.tradeStartDate.heading")
      document.getElementById("form-hint-id").text() shouldBe Messages("common.date.hint.example")
      document.getElementById("question-text-id").text() shouldBe Messages("page.seis.companyDetails.TradeStartDate.question.hint")
      document.getElementById("question-text-id").hasClass("h2-heading")
      document.getElementById("question-date-text-legend-id").hasClass("visuallyhidden")
      document.getElementById("hasTradeStartDate-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasTradeStartDate-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.DateOfIncorporationController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }
}
