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

import auth.{MockAuthConnector, MockConfigEISFlow}
import common.KeystoreKeys
import controllers.eis.{HadOtherInvestmentsController, routes}
import models.{HadOtherInvestmentsModel, HadPreviousRFIModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class HadOtherInvestmentsSpec extends ViewSpec {

  object TestController extends HadOtherInvestmentsController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(hadOtherInvestmentsModel: Option[HadOtherInvestmentsModel] = None,
                 hadPreviousRFIModel: Option[HadPreviousRFIModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkHadRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(controllers.eis.routes.HadPreviousRFIController.show().url)))
    when(mockS4lConnector.fetchAndGetFormData[HadOtherInvestmentsModel](Matchers.eq(KeystoreKeys.hadOtherInvestments))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hadOtherInvestmentsModel))
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hadPreviousRFIModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkReviewPreviousSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(controllers.eis.routes.ProposedInvestmentController.show().url)))
  }


  "Verify that the hadOtherInvestments page contains the correct elements " +
    "when a valid hadOtherInvestmentsModel is passed as returned from keystore" in new Setup {
    val document : Document = {
      setupMocks(Some(hadOtherInvestmentsModelYes),Some(hadPreviousRFIModelYes))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.title() shouldBe Messages("page.previousInvestment.hadOtherInvestments.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.hadOtherInvestments.heading")
    document.select("#hadOtherInvestments-yes").size() shouldBe 1
    document.select("#hadOtherInvestments-no").size() shouldBe 1
    document.getElementById("hadOtherInvestments-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("hadOtherInvestments-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("hadOtherInvestments-legend").select(".visuallyhidden").text() shouldBe Messages("page.previousInvestment.hadOtherInvestments.heading")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.two")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that hadOtherInvestments page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new Setup {
    val document : Document = {
      setupMocks(None, Some(hadPreviousRFIModelYes))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.title() shouldBe Messages("page.previousInvestment.hadOtherInvestments.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.hadOtherInvestments.heading")
    document.select("#hadOtherInvestments-yes").size() shouldBe 1
    document.select("#hadOtherInvestments-no").size() shouldBe 1
    document.getElementById("hadOtherInvestments-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("hadOtherInvestments-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("hadOtherInvestments-legend").select(".visuallyhidden").text() shouldBe Messages("page.previousInvestment.hadOtherInvestments.heading")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.two")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that hadOtherInvestments page contains show the error summary when an invalid model (no radio button selection) is submitted" in new Setup {
    setupMocks(None, Some(hadPreviousRFIModelYes))

    val document : Document = {
      // submit the model with no radio selected as a post action
      val result = TestController.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.previousInvestment.hadOtherInvestments.title")

  }
}
