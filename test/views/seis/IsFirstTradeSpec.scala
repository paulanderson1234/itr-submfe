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

import auth.{MockAuthConnector, MockConfigSingleFlow}
import common.{Constants, KeystoreKeys}
import connectors.SubmissionConnector
import controllers.seis.IsFirstTradeController
import models.IsFirstTradeModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class IsFirstTradeSpec extends ViewSpec {

  object TestController extends IsFirstTradeController {
    override lazy val applicationConfig = MockConfigSingleFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
   // override lazy val submissionConnector: SubmissionConnector = mockSubmissionConnector
  }

  def setupMocks(isFirstTradeModel: Option[IsFirstTradeModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[IsFirstTradeModel](Matchers.eq(KeystoreKeys.isFirstTrade))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(isFirstTradeModel))

  "The Is this the first trade your company has carried out page" should {

    "Verify that the Is First Trade page contains the correct elements when a valid IsFirstTRadeModel is passed from keystore" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(isFirstTradeIModelYes))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.seis.companyDetails.isFirstTrade.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.seis.companyDetails.isFirstTrade.heading")
      document.getElementById("main-heading").hasClass("h1-heading")
      document.getElementById("isFirstTrade-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("isFirstTrade-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.TradeStartDateController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("isFirstTrade-legend").hasClass("visuallyhidden")
      document.getElementById("isFirstTrade-legend").text shouldBe Messages("page.seis.companyDetails.isFirstTrade.legend")
    }


    "Verify that the Is First Trade page contains the correct elements when an invalid IsFirstTradeModel is passed" in new SEISSetup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.seis.companyDetails.isFirstTrade.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.seis.companyDetails.isFirstTrade.heading")
      document.getElementById("isFirstTrade-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("isFirstTrade-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.TradeStartDateController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("isFirstTrade-legend").hasClass("visuallyhidden")
      document.getElementById("isFirstTrade-legend").text shouldBe Messages("page.seis.companyDetails.isFirstTrade.legend")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.getElementById("isFirstTrade-error-summary")

    }
  }
}
