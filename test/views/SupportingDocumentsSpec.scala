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
import controllers.helpers.FakeRequestHelper
import controllers.{SupportingDocumentsController, routes}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class SupportingDocumentsSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockS4lConnector = mock[S4LConnector]

  class SetupPage {
    val controller = new SupportingDocumentsController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val s4lConnector: S4LConnector = mockS4lConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  "The Supporting documents page" should {

    "Verify that the Supporting documents page contains the correct elements" in new SetupPage {
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSupportingDocs))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ConfirmCorrespondAddressController.show().toString())))

      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        val result = controller.show.apply((authorisedFakeRequest))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.supportingDocuments.SupportingDocuments.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.checkAnswers")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ConfirmCorrespondAddressController.show.toString()
      document.getElementById("description-one").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.text.one")
      document.getElementById("company-accounts").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.one")
      document.getElementById("company-control").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.two")
      document.getElementById("articles").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.three")
      document.getElementById("attract-docs").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.four")
      document.getElementById("description-two").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.text.two")
      document.getElementById("description-three").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.text.three")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.five")

    }
  }

  "The Supporting documents page" should {

    "Verify that the Supporting documents page contains the correct elements cc" in new SetupPage {
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSupportingDocs))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(routes.TaxpayerReferenceController.show().toString())))

      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        val result = controller.show.apply((authorisedFakeRequest))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.supportingDocuments.SupportingDocuments.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.checkAnswers")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.TaxpayerReferenceController.show.toString()
      document.getElementById("description-one").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.text.one")
      document.getElementById("company-accounts").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.one")
      document.getElementById("company-control").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.two")
      document.getElementById("articles").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.three")
      document.getElementById("attract-docs").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.four")
      document.getElementById("description-two").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.text.two")
      document.getElementById("description-three").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.text.three")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.five")

    }
  }
}
