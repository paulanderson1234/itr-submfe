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

import connectors.KeystoreConnector
import controllers.helpers.FakeRequestHelper
import controllers.{CommercialSaleController, TenYearPlanController, routes}
import models.{CommercialSaleModel, TenYearPlanModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class TenYearPlanSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockKeystoreConnector = mock[KeystoreConnector]

  val yesWithTenYearPlanModel = TenYearPlanModel("Yes", Some("abcd"))
  val noWithNoTenYearPlanModel = TenYearPlanModel("No", None)
  val yesInvalidTenYearPlanModel = TenYearPlanModel("Yes", None)
  val emptyTenYearPlanModel = new TenYearPlanModel("", None)

  class SetupPage {

    val controller = new TenYearPlanController {
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "The Ten Year Plan page" should {

    "Verify that the ten year plan page contains the correct elements when a valid 'Yes' TenYearPlanModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(yesWithTenYearPlanModel)))
        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "hasTenYearPlan" -> "Yes",
          "tenYearPlanDesc" -> "abcd"
        ))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.heading")
      document.getElementById("desc-one").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.one")
      document.getElementById("tenYearPlanDesc-hint").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.two")
      document.getElementById("tenYearPlanDesc-desc").hasClass("page.knowledgeIntensive.TenYearPlan.description.one")
      document.getElementById("message-text-id").hasClass("page.knowledgeIntensive.TenYearPlan.description.two")
      document.getElementById("hasTenYearPlan-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasTenYearPlan-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.PercentageStaffWithMastersController.show().toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
    }


    "Verify that the ten year plan page contains the correct elements when a valid 'No' TenYearPlanModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(noWithNoTenYearPlanModel)))
        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "hasTenYearPlan" -> "Yes",
          "tenYearPlanDesc" -> "abcd"
        ))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.heading")
      document.getElementById("desc-one").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.one")
      document.getElementById("tenYearPlanDesc-hint").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.two")
      document.getElementById("tenYearPlanDesc-desc").hasClass("page.knowledgeIntensive.TenYearPlan.description.one")
      document.getElementById("message-text-id").hasClass("page.knowledgeIntensive.TenYearPlan.description.two")
      document.getElementById("hasTenYearPlan-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasTenYearPlan-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.PercentageStaffWithMastersController.show().toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
    }

    "Verify that the ten year plan page contains the correct elements when an invalid TenYearPlanModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(emptyTenYearPlanModel)))
        val result = controller.submit.apply(fakeRequestWithSession)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.heading")
      document.getElementById("desc-one").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.one")
      document.getElementById("tenYearPlanDesc-hint").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.two")
      document.getElementById("tenYearPlanDesc-desc").hasClass("page.knowledgeIntensive.TenYearPlan.description.one")
      document.getElementById("message-text-id").hasClass("page.knowledgeIntensive.TenYearPlan.description.two")
      document.getElementById("hasTenYearPlan-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasTenYearPlan-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.PercentageStaffWithMastersController.show().toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

    "Verify that the commercial sale page contains the correct elements when an invalid 'Yes' TenYearPlanModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(yesInvalidTenYearPlanModel)))
        val result = controller.submit.apply(fakeRequestWithSession)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.heading")
      document.getElementById("desc-one").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.one")
      document.getElementById("tenYearPlanDesc-hint").text() shouldBe Messages("page.knowledgeIntensive.TenYearPlan.question.hint.two")
      document.getElementById("tenYearPlanDesc-desc").hasClass("page.knowledgeIntensive.TenYearPlan.description.one")
      document.getElementById("message-text-id").hasClass("page.knowledgeIntensive.TenYearPlan.description.two")
      document.getElementById("hasTenYearPlan-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasTenYearPlan-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.PercentageStaffWithMastersController.show().toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }
}
