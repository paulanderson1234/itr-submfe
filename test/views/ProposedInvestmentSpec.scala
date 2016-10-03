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
import connectors.{EnrolmentConnector, KeystoreConnector, SubmissionConnector}
import controllers.{ProposedInvestmentController, routes}
import controllers.helpers.FakeRequestHelper
import models.ProposedInvestmentModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class ProposedInvestmentSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockKeystoreConnector = mock[KeystoreConnector]
  val mockSubmissionConnector = mock[SubmissionConnector]

  val proposedInvestmentModel = new ProposedInvestmentModel(5000000)
  val emptyProposedInvestmentModel = new ProposedInvestmentModel(0)

  class SetupPage {

    val controller = new ProposedInvestmentController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
      val submissionConnector: SubmissionConnector = mockSubmissionConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  "The Proposed Investment page" should {

    "Verify that the proposed investment page contains the correct elements when a valid ProposedInvestmentModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[ProposedInvestmentModel]
          (Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(proposedInvestmentModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(routes.TenYearPlanController.show().toString())))

        val result = controller.show.apply((authorisedFakeRequest.withFormUrlEncodedBody(
          "investmentAmount" -> "5000000"
        )))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.investment.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount-hint").text() shouldBe Messages("page.investment.amount.hint")
      document.getElementById("help").text() shouldBe Messages("page.investment.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.investment.help.link.text")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.TenYearPlanController.show.toString()
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    }

    "Verify that the proposed investment page contains the correct elements when an invalid ProposedInvestmentModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[ProposedInvestmentModel]
          (Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(emptyProposedInvestmentModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(routes.HadPreviousRFIController.show().toString())))

        val result = controller.submit.apply((authorisedFakeRequest))
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount-hint").text() shouldBe Messages("page.investment.amount.hint")
      document.getElementById("help").text() shouldBe Messages("page.investment.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.investment.help.link.text")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.HadPreviousRFIController.show.toString()
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

    "Verify that the proposed investment page contains the correct elements when an None ProposedInvestmentModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[ProposedInvestmentModel]
          (Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))

        val result = controller.submit.apply((authorisedFakeRequest))
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.investment.amount.heading")
      document.getElementById("label-amount-hint").text() shouldBe Messages("page.investment.amount.hint")
      document.getElementById("help").text() shouldBe Messages("page.investment.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.investment.help.link.text")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show.toString()
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }

}
