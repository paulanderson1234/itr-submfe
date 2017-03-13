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
import controllers.eis.UsedInvestmentReasonBeforeController
import models.UsedInvestmentReasonBeforeModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class UsedInvestmentReasonBeforeSpec extends ViewSpec {

  object TestController extends UsedInvestmentReasonBeforeController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }
  
  def setupMocks(usedInvestmentReasonBeforeModel: Option[UsedInvestmentReasonBeforeModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(usedInvestmentReasonBeforeModel))

  "Verify that the UsedInvestmentReasonBefore page contains the correct elements " +
    "when a valid UsedInvestmentReasonBeforeModel is passed as returned from keystore" in new Setup {
    val document : Document = {
      setupMocks(Some(usedInvestmentReasonBeforeModelYes))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.ProposedInvestmentController.show().url
    document.title() shouldBe Messages("page.investment.UsedInvestmentReasonBefore.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.UsedInvestmentReasonBefore.heading")
    document.select("#usedInvestmentReasonBefore-yes").size() shouldBe 1
    document.select("#usedInvestmentReasonBefore-no").size() shouldBe 1
    document.getElementById("usedInvestmentReasonBefore-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("usedInvestmentReasonBefore-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that UsedInvestmentReasonBefore page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new Setup {
    val document : Document = {
      setupMocks()
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.ProposedInvestmentController.show().url
    document.title() shouldBe Messages("page.investment.UsedInvestmentReasonBefore.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.UsedInvestmentReasonBefore.heading")
    document.select("#usedInvestmentReasonBefore-yes").size() shouldBe 1
    document.select("#usedInvestmentReasonBefore-no").size() shouldBe 1
    document.getElementById("usedInvestmentReasonBefore-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("usedInvestmentReasonBefore-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that UsedInvestmentReasonBefore page contains error summary when invalid model is submitted" in new Setup {
    val document : Document = {
      // submit the model with no radio selected as a post action
      val result = TestController.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.investment.UsedInvestmentReasonBefore.title")
  }
}
