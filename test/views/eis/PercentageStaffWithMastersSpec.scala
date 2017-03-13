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
import controllers.eis.PercentageStaffWithMastersController
import models.PercentageStaffWithMastersModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class PercentageStaffWithMastersSpec extends ViewSpec {

  object TestController extends PercentageStaffWithMastersController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(percentageStaffWithMastersModel: Option[PercentageStaffWithMastersModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(percentageStaffWithMastersModel))

  "Verify that the PercentageStaffWithMasters page contains the correct elements " +
    "when a valid PercentageStaffWithMastersModel is passed as returned from keystore" in new Setup {
    val document : Document = {
      setupMocks(Some(percentageStaffWithMastersModelYes))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.OperatingCostsController.show().url
    document.title() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.heading")
    document.getElementById("condition-for-KI").text() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.hint")
    document.select("#staffWithMasters-yes").size() shouldBe 1
    document.select("#staffWithMasters-no").size() shouldBe 1
    document.getElementById("staffWithMasters-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("staffWithMasters-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
    document.getElementById("yes-hint").text() shouldBe Messages("page.percentageStaffWithMasters.yes.hint")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that PercentageStaffWithMasters page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new Setup {
    val document : Document = {
      setupMocks()
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.OperatingCostsController.show().url
    document.title() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.heading")
    document.getElementById("condition-for-KI").text() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.hint")
    document.select("#staffWithMasters-yes").size() shouldBe 1
    document.select("#staffWithMasters-no").size() shouldBe 1
    document.getElementById("staffWithMasters-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("staffWithMasters-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }


  "Verify that PercentageStaffWithMasters page contains error summary when invalid model is submitted" in new Setup {
    val document : Document = {
      // submit the model with no radio slected as a post action
      val result = TestController.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.title")
  }
}
