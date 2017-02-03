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

import auth.MockAuthConnector
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.seis.{HadPreviousRFIController, routes}
import models.HadPreviousRFIModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
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

  def setupMocks(backLink: Option[String] = None, hadPreviousRFIModel: Option[HadPreviousRFIModel] = None): Unit = {
//    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
//      .thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hadPreviousRFIModel))
//    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkReviewPreviousSchemes))
//      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(routes.ProposedInvestmentController.show().url)))
  }


  "Verify that the hadPreviousRFI page contains the correct elements " +
    "when a valid HadPreviousRFIModel is passed as returned from keystore" in new Setup {
    val document : Document = {
      //TODO: to replace when ProposedInvestmentController is made available
//      setupMocks(Some(routes.ProposedInvestmentController.show().url), Some(hadPreviousRFIModelYes))
      //TODO: replace with above when new controller available
      setupMocks(Some(routes.HadPreviousRFIController.show().url), Some(hadPreviousRFIModelYes))

      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
//TODO: relace when ProposedInvestment available... document.body.getElementById("back-link").attr("href") shouldEqual routes.ProposedInvestmentController.show().url
    document.body.getElementById("back-link").attr("href") shouldEqual routes.HadPreviousRFIController.show().url
    document.title() shouldBe Messages("page.previousInvestment.hadPreviousRFI.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
    document.select("#hadPreviousRFI-yes").size() shouldBe 1
    document.select("#hadPreviousRFI-yes").size() shouldBe 1
    document.getElementById("hadPreviousRFI-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("hadPreviousRFI-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("hadPreviousRFI-legend").select(".visuallyhidden").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.two")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that hadPreviousRFI page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new Setup {
    val document : Document = {
      //TODO: to replace when ProposedInvestmentController is available
//      setupMocks(Some(routes.ProposedInvestmentController.show().url))
      //TODO: replace with above when new controller available
      setupMocks(Some(routes.HadPreviousRFIController.show().url))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    //TODO: replace when ProposedInvestment available...document.body.getElementById("back-link").attr("href") shouldEqual routes.ProposedInvestmentController.show().url
    document.body.getElementById("back-link").attr("href") shouldEqual routes.HadPreviousRFIController.show().url
    document.title() shouldBe Messages("page.previousInvestment.hadPreviousRFI.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
    document.select("#hadPreviousRFI-yes").size() shouldBe 1
    document.select("#hadPreviousRFI-no").size() shouldBe 1
    document.getElementById("hadPreviousRFI-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("hadPreviousRFI-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("hadPreviousRFI-legend").select(".visuallyhidden").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.two")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that HadPreviousRFI page contains show the error summary when an invalid model (no radio button selection) is submitted" in new Setup {
    //TODO: replace with this when ProposedInvestmentController available
//    setupMocks(Some(routes.ProposedInvestmentController.show().url), Some(hadPreviousRFIModelYes))

    //TODO: remove once above is available
    setupMocks(Some(routes.HadPreviousRFIController.show().url), Some(hadPreviousRFIModelYes))

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