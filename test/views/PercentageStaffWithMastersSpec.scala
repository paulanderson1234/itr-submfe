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

import java.util.UUID

import auth.{Enrolment, Identifier, MockAuthConnector}
import builders.SessionBuilder
import common.Constants
import config.FrontendAppConfig
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.FakeRequestHelper
import controllers.{PercentageStaffWithMastersController, routes}
import models.PercentageStaffWithMastersModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class PercentageStaffWithMastersSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockS4lConnector = mock[S4LConnector]
  val mockSubmissionConnector = mock[SubmissionConnector]

  val percentageStaffWithMastersModel = new PercentageStaffWithMastersModel(Constants.StandardRadioButtonYesValue)
  val emptyPercentageStaffWithMastersModel = new PercentageStaffWithMastersModel("")

  class SetupPage {

    val controller = new PercentageStaffWithMastersController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val s4lConnector: S4LConnector = mockS4lConnector
      val submissionConnector: SubmissionConnector = mockSubmissionConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  "Verify that the PercentageStaffWithMasters page contains the correct elements " +
    "when a valid PercentageStaffWithMastersModel is passed as returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockS4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(percentageStaffWithMastersModel)))
      val result = controller.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.OperatingCostsController.show().url
    document.title() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.heading")
    document.getElementById("condition-for-KI").text() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.hint")
    document.select("#staffWithMasters-yes").size() shouldBe 1
    document.select("#staffWithMasters-no").size() shouldBe 1
    document.getElementById("staffWithMasters-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("staffWithMasters-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that PercentageStaffWithMasters page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockS4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(emptyPercentageStaffWithMastersModel)))
      val result = controller.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.OperatingCostsController.show().url
    document.title() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.heading")
    document.getElementById("condition-for-KI").text() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.hint")
    document.select("#staffWithMasters-yes").size() shouldBe 1
    document.select("#staffWithMasters-no").size() shouldBe 1
    document.getElementById("staffWithMasters-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("staffWithMasters-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }


  "Verify that PercentageStaffWithMasters page contains error summary when invalid model is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      // submit the model with no radio slected as a post action
      val result = controller.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.knowledgeIntensive.PercentageStaffWithMasters.title")

  }
}
