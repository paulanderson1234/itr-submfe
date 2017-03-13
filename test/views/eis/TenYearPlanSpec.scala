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
import controllers.eis.TenYearPlanController
import models.TenYearPlanModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class TenYearPlanSpec extends ViewSpec {
  
  val invalidTenYearPlanModel = TenYearPlanModel("Yes", None)

  object TestController extends TenYearPlanController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(tenYearPlanModel: Option[TenYearPlanModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(tenYearPlanModel))

  "The Ten Year Plan page" should {

    "Verify that the ten year plan page contains the correct elements when a valid 'Yes' TenYearPlanModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(tenYearPlanModelYes))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.heading")
      document.getElementById("desc-one").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.one")
      document.getElementById("desc-two").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.two")
      document.getElementById("infoId").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.three")
      document.getElementById("infoId-2").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.four")
      document.getElementById("labelTextId").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.description.one")
      document.getElementById("labelTextId").hasClass("visuallyhidden")
      document.getElementById("hasTenYearPlan-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasTenYearPlan-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.PercentageStaffWithMastersController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
    }

    "Verify that the ten year plan page contains the correct elements when a valid 'No' TenYearPlanModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(tenYearPlanModelNo))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.heading")
      document.getElementById("desc-one").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.one")
      document.getElementById("desc-two").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.two")
      document.getElementById("hasTenYearPlan-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasTenYearPlan-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.PercentageStaffWithMastersController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
    }

    "Verify that the ten year plan page contains the correct elements when an invalid TenYearPlanModel is passed" in new Setup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.heading")
      document.getElementById("desc-one").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.one")
      document.getElementById("desc-two").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.two")
      document.getElementById("hasTenYearPlan-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasTenYearPlan-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.PercentageStaffWithMastersController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("infoId").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.three")
      document.getElementById("infoId-2").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.four")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

    "Verify that the commercial sale page contains the correct elements when an invalid 'Yes' TenYearPlanModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(invalidTenYearPlanModel))
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.heading")
      document.getElementById("desc-one").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.one")
      document.getElementById("hasTenYearPlan-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasTenYearPlan-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.PercentageStaffWithMastersController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("labelTextId").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.description.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }
}
