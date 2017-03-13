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
import controllers.eis.IneligibleForKIController
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class IneligibleForKISpec extends ViewSpec {

  object TestController extends IneligibleForKIController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(backUrl: Option[String] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[String]
      (Matchers.eq(KeystoreKeys.backLinkIneligibleForKI))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backUrl))

  "The Ineligible for Knowledge Intensive page" should {

    "Verify that the Ineligible for Knowledge Intensive page contains the correct elements with valid backlink" in new Setup {
      val document: Document = {
        setupMocks(Some(controllers.eis.routes.OperatingCostsController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.OperatingCostsController.show().url
      document.title shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.IneligibleForKI.heading")
      document.getElementById("description-one").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.description.one")
      document.getElementById("description-two").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.description.two")
      document.getElementById("reason-one").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.bullet.one")
      document.getElementById("reason-two").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.bullet.two")
      document.getElementById("reason-three").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.bullet.three")
      document.getElementById("next").text() shouldEqual Messages("common.button.continue")
    }

    "The Ineligible for Knowledge Intensive page" should {

      "Verify that the Ineligible for Knowledge Intensive page contains the correct elements with valid alternate backlink" in new Setup {
        val document: Document = {
          setupMocks(Some(controllers.eis.routes.TenYearPlanController.show().url))
          val result = TestController.show.apply(authorisedFakeRequest)
          Jsoup.parse(contentAsString(result))
        }
        document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.TenYearPlanController.show().url
        document.title shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.title")
        document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.IneligibleForKI.heading")
        document.getElementById("description-one").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.description.one")
        document.getElementById("description-two").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.description.two")
        document.getElementById("reason-one").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.bullet.one")
        document.getElementById("reason-two").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.bullet.two")
        document.getElementById("reason-three").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.bullet.three")
        document.getElementById("next").text() shouldEqual Messages("common.button.continue")
      }

    }

    "The Ineligible for Knowledge Intensive page" should {

      "Verify that the Ineligible for Knowledge Intensive page contains the correct elements " +
        "and sets backlink to any link returned from keystore" in new Setup {
        val document: Document = {
          setupMocks(Some(routes.ApplicationHubController.show().url))
          val result = TestController.show.apply(authorisedFakeRequest)
          Jsoup.parse(contentAsString(result))
        }
        document.body.getElementById("back-link").attr("href") shouldEqual routes.ApplicationHubController.show().url
        document.title shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.title")
        document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.IneligibleForKI.heading")
        document.getElementById("description-one").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.description.one")
        document.getElementById("description-two").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.description.two")
        document.getElementById("reason-one").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.bullet.one")
        document.getElementById("reason-two").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.bullet.two")
        document.getElementById("reason-three").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.bullet.three")
        document.getElementById("next").text() shouldEqual Messages("common.button.continue")
      }

    }
  }
}
