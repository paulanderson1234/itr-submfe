/*
 * Copyright 2016 HM Revenue & Customs
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
import controllers.{HadPreviousRFIController, routes}
import models.HadPreviousRFIModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class HadPreviousRFISpec extends ViewSpec {

  object TestController extends HadPreviousRFIController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(hadPreviousRFIModel: Option[HadPreviousRFIModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(hadPreviousRFIModel))

  "Verify that the hadPreviousRFI page contains the correct elements " +
    "when a valid HadPreviousRFIModel is passed as returned from keystore" in new Setup {
    val document : Document = {
      setupMocks(Some(hadPreviousRFIModelYes))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual routes.SubsidiariesController.show().url
    document.title() shouldBe Messages("page.previousInvestment.hadPreviousRFI.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
    document.select("#hadPreviousRFI-yes").size() shouldBe 1
    document.select("#hadPreviousRFI-yes").size() shouldBe 1
    document.getElementById("hadPreviousRFI-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("hadPreviousRFI-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("hadPreviousRFILegend").select(".visuallyhidden").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.two")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that hadPreviousRFI page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new Setup {
    val document : Document = {
      setupMocks()
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual routes.SubsidiariesController.show().url
    document.title() shouldBe Messages("page.previousInvestment.hadPreviousRFI.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
    document.select("#hadPreviousRFI-yes").size() shouldBe 1
    document.select("#hadPreviousRFI-no").size() shouldBe 1
    document.getElementById("hadPreviousRFI-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("hadPreviousRFI-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("hadPreviousRFILegend").select(".visuallyhidden").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.two")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that HadPreviousRFI page contains show the error summary when an invalid model (no radio button selection) is submitted" in new Setup {
    val document : Document = {
      // submit the model with no radio selected as a post action
      val result = TestController.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.previousInvestment.hadPreviousRFI.title")

  }
}

