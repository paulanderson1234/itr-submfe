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
import common.Constants
import config.FrontendAppConfig
import connectors.{EnrolmentConnector, KeystoreConnector}
import controllers.{SubsidiariesNinetyOwnedController, TaxpayerReferenceController, routes}
import controllers.helpers.FakeRequestHelper
import models.{SubsidiariesNinetyOwnedModel, TaxpayerReferenceModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class SubsidiariesNinetyOwnedSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val subsidiariesNinetyOwnedModel = new SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonYesValue)
  val emptyTaxpayerReferenceModel = new SubsidiariesNinetyOwnedModel("")

  class SetupPage {

    val controller = new SubsidiariesNinetyOwnedController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  "The Subsidiaries Ninety Owned page" should {

    "Verify that the Subsidiaries Ninety Owned page contains the correct elements when a valid SubsidiariesNinetyOwnedModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesNinetyOwnedModel)))
        val result = controller.show.apply(authorisedFakeRequestToPOST(
          "ownNinetyPercent" -> Constants.StandardRadioButtonYesValue
        ))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.heading")
      document.getElementById("text-one-id").text() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.error.one")
      document.getElementById("text-two-id").text() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.error.two")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SubsidiariesSpendingInvestmentController.show().toString()
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    }

    "Verify that the taxpayer reference page contains the correct elements when an invalid TaxpayerReferenceModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesNinetyOwnedModel)))
        val result = controller.submit.apply(authorisedFakeRequestToPOST(
          "ownNinetyPercent" -> ""
        ))
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.heading")
      document.getElementById("text-one-id").text() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.error.one")
      document.getElementById("text-two-id").text() shouldBe Messages("page.investment.SubsidiariesNinetyOwned.error.two")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SubsidiariesSpendingInvestmentController.show().toString()
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }
  }
}
