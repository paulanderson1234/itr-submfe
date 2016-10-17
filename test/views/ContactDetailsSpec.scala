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
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.FakeRequestHelper
import controllers.{ContactDetailsController, routes}
import models.ContactDetailsModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class ContactDetailsSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockS4lConnector = mock[S4LConnector]

  val contactDetailsModel = new ContactDetailsModel("Jeff","Stelling","01384 555678","Jeff.Stelling@HMRC.gov.uk")
  val emptyContactDetailsModel = new ContactDetailsModel("","","","")

  class SetupPage {

    val controller = new ContactDetailsController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val s4lConnector: S4LConnector = mockS4lConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  "The Contact Details page" should {

    "Verify that the contact details page contains the correct elements when a valid ContactDetailsModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(contactDetailsModel)))
        val result = controller.show.apply(authorisedFakeRequest.withFormUrlEncodedBody(
          "forename" -> "Jeff",
          "surname" -> "Stelling",
          "telephoneNumber" -> "01384 555678",
          "email" -> "Jeff.Stelling@HMRC.gov.uk"))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.contactInformation.contactDetails.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.contactDetails.heading")
      document.getElementById("label-forename").text() shouldBe Messages("page.contactInformation.contactDetails.forename.label")
      document.getElementById("label-surname").text() shouldBe Messages("page.contactInformation.contactDetails.surname.label")
      document.getElementById("label-telephoneNumber").text() shouldBe Messages("page.contactInformation.contactDetails.phoneNumber.label")
      document.getElementById("label-email").text() shouldBe Messages("page.contactInformation.contactDetails.email.label")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.InvestmentGrowController.show().toString()
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.four")
    }

    "Verify that the proposed investment page contains the correct elements when an invalid ContactDetailsModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(emptyContactDetailsModel)))
        val result = controller.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.contactInformation.contactDetails.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.contactDetails.heading")
      document.getElementById("label-forename").text() contains Messages("page.contactInformation.contactDetails.forename.label")
      document.getElementById("label-surname").text() contains Messages("page.contactInformation.contactDetails.surname.label")
      document.getElementById("label-telephoneNumber").text() contains Messages("page.contactInformation.contactDetails.phoneNumber.label")
      document.getElementById("label-email").text() contains Messages("page.contactInformation.contactDetails.email.label")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.InvestmentGrowController.show().toString()
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.four")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }

}
