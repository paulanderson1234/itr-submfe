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
import common.KeystoreKeys
import config.FrontendAppConfig
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.{IneligibleForKIController, routes}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class IneligibleForKISpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockS4lConnector = mock[S4LConnector]

  class SetupPage {

    val controller = new IneligibleForKIController {
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val s4lConnector: S4LConnector = mockS4lConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  "The Ineligible for Knowledge Intensive page" should {

    "Verify that the Ineligible for Knowledge Intensive page contains the correct elements with valid backlink" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockS4lConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkIneligibleForKI))(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(Option(routes.OperatingCostsController.show().url)))

        val result = controller.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }

      document.body.getElementById("back-link").attr("href") shouldEqual routes.OperatingCostsController.show().url
      document.title shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.IneligibleForKI.heading")
      document.getElementById("description-one").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.description.one")
      document.getElementById("description-two").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.description.two")
      document.getElementById("next").text() shouldEqual Messages("common.button.continueNextSection")
    }

    "The Ineligible for Knowledge Intensive page" should {

      "Verify that the Ineligible for Knowledge Intensive page contains the correct elements with valid alternate backlink" in new SetupPage {
        val document: Document = {
          val userId = s"user-${UUID.randomUUID}"

          when(mockS4lConnector.fetchAndGetFormData[String]
            (Matchers.eq(KeystoreKeys.backLinkIneligibleForKI))(Matchers.any(), Matchers.any(),Matchers.any()))
            .thenReturn(Future.successful(Option(routes.TenYearPlanController.show().url)))

          val result = controller.show.apply(authorisedFakeRequest)
          Jsoup.parse(contentAsString(result))
        }

        document.body.getElementById("back-link").attr("href") shouldEqual routes.TenYearPlanController.show().url
        document.title shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.title")
        document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.IneligibleForKI.heading")
        document.getElementById("description-one").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.description.one")
        document.getElementById("description-two").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.description.two")
        document.getElementById("next").text() shouldEqual Messages("common.button.continueNextSection")
      }

    }

    "The Ineligible for Knowledge Intensive page" should {

      "Verify that the Ineligible for Knowledge Intensive page contains the correct elements " +
        "and sets backlink to any link returnd from keystorek" in new SetupPage {
        val document: Document = {
          val userId = s"user-${UUID.randomUUID}"

          when(mockS4lConnector.fetchAndGetFormData[String]
            (Matchers.eq(KeystoreKeys.backLinkIneligibleForKI))(Matchers.any(), Matchers.any(),Matchers.any()))
            .thenReturn(Future.successful(Option(routes.HowToApplyController.show().url)))

          val result = controller.show.apply(authorisedFakeRequest)
          Jsoup.parse(contentAsString(result))
        }

        document.body.getElementById("back-link").attr("href") shouldEqual routes.HowToApplyController.show().url
        document.title shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.title")
        document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.IneligibleForKI.heading")
        document.getElementById("description-one").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.description.one")
        document.getElementById("description-two").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.description.two")
        document.getElementById("next").text() shouldEqual Messages("common.button.continueNextSection")
      }

    }
  }
}
