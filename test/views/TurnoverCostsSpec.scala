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
  import config.FrontendAppConfig
  import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
  import controllers.helpers.FakeRequestHelper
  import controllers.{TurnoverCostsController, routes}
  import models.AnnualTurnoverCostsModel
  import org.jsoup.Jsoup
  import org.jsoup.nodes.Document
  import org.mockito.Matchers
  import org.mockito.Mockito._
  import org.scalatest.mock.MockitoSugar
  import play.api.i18n.Messages
  import play.api.test.Helpers._
  import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

  import scala.concurrent.Future

  class TurnoverCostsSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

    val mockS4lConnector = mock[S4LConnector]
    val mockSubmissionConnector = mock[SubmissionConnector]

    val operatingCostsModel = AnnualTurnoverCostsModel("750000", "800000", "934000", "231000", "340000")
    val emptyTurnoverCostsModel = new AnnualTurnoverCostsModel("", "", "", "", "")

    class SetupPage {

      val controller = new TurnoverCostsController{
        override lazy val applicationConfig = FrontendAppConfig
        override lazy val authConnector = MockAuthConnector
        val s4lConnector: S4LConnector = mockS4lConnector
        val submissionConnector: SubmissionConnector = mockSubmissionConnector
        override lazy val enrolmentConnector = mock[EnrolmentConnector]
      }
      when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
    }

    "Verify that the TurnoverCosts page contains the correct elements " +
      "when a valid TurnoverCostsModel is passed as returned from keystore" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(Option(operatingCostsModel)))
        val result = controller.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }

      document.body.getElementById("back-link").attr("href") shouldEqual routes.NewProductController.show().url
      document.title() shouldBe Messages("page.companyDetails.TurnoverCosts.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.TurnoverCosts.heading")
      document.getElementById("turnover-costs-hint-one").text() shouldBe Messages("page.companyDetails.TurnoverCosts.hint.one")
      document.getElementById("col-heading-one").hasClass("visuallyhidden")
      document.getElementById("col-heading-one").text() shouldBe Messages("page.companyDetails.TurnoverCosts.col.heading.one")
      document.getElementById("col-heading-two").hasClass("visuallyhidden")
      document.getElementById("col-heading-two").text() shouldBe Messages("page.companyDetails.TurnoverCosts.col.heading.two")

      document.getElementById("help").text() shouldBe Messages("page.companyDetails.TurnoverCosts.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.companyDetails.TurnoverCosts.help.text")

      document.getElementById("row-heading-one").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.one")
      document.getElementById("row-heading-two").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.two")
      document.getElementById("row-heading-three").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.three")
      document.getElementById("row-heading-four").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.four")
      document.getElementById("row-heading-five").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.five")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")

    }

    "Verify that TurnoverCosts page contains the correct elements when an empty model " +
      "is passed because nothing was returned from keystore" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(None))
        val result = controller.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }

      document.body.getElementById("back-link").attr("href") shouldEqual routes.NewProductController.show().url
      document.title() shouldBe Messages("page.companyDetails.TurnoverCosts.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.TurnoverCosts.heading")
      document.getElementById("turnover-costs-hint-one").text() shouldBe Messages("page.companyDetails.TurnoverCosts.hint.one")
      document.getElementById("col-heading-one").hasClass("visuallyhidden")
      document.getElementById("col-heading-one").text() shouldBe Messages("page.companyDetails.TurnoverCosts.col.heading.one")
      document.getElementById("col-heading-two").hasClass("visuallyhidden")
      document.getElementById("col-heading-two").text() shouldBe Messages("page.companyDetails.TurnoverCosts.col.heading.two")

      document.getElementById("help").text() shouldBe Messages("page.companyDetails.TurnoverCosts.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.companyDetails.TurnoverCosts.help.text")

      document.getElementById("row-heading-one").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.one")
      document.getElementById("row-heading-two").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.two")
      document.getElementById("row-heading-three").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.three")
      document.getElementById("row-heading-four").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.four")
      document.getElementById("row-heading-five").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.five")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
    }

    "Verify that TurnoverCosts page contains show the error summary when an invalid model (no data) is submitted" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        // submit the model with no radio slected as a post action
        val result = controller.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }

      // Make sure we have the expected error summary displayed
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.title() shouldBe Messages("page.companyDetails.TurnoverCosts.title")

    }
  }
