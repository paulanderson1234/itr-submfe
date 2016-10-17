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
import controllers.{DateOfIncorporationController, routes}
import controllers.helpers.{FakeRequestHelper, TestHelper}
import models.DateOfIncorporationModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class DateOfIncorporationSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockS4lConnector = mock[S4LConnector]

  val contactDetailsModel = new DateOfIncorporationModel(Some(23), Some(11), Some(1993))
  val emptyDateOfIncorporationModel = new DateOfIncorporationModel(None, None, None)

  class SetupPage {

    val controller = new DateOfIncorporationController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val s4lConnector: S4LConnector = mockS4lConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  "The Date Of Incorporation page" should {

    "Verify that date of incorporation page contains the correct elements when a valid DateOfIncorporationModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(contactDetailsModel)))
        val result = controller.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.companyDetails.DateOfIncorporation.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.DateOfIncorporation.heading")
      document.body.getElementsByClass("form-hint").text should include(Messages("date.hint.dateOfIncorporation"))
      document.body.getElementById("incorporationDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("incorporationMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("incorporationYear").parent.text shouldBe Messages("common.date.fields.year")
      document.body.getElementById("date-of-incorporation-where-to-find").parent.text should include
      (Messages("page.companyDetails.DateOfIncorporation.location"))
      document.body.getElementById("company-house-db").text() shouldEqual TestHelper.getExternalLinkText(Messages("page.companyDetails.DateOfIncorporation.companiesHouse"))
      document.body.getElementById("company-house-db").attr("href") shouldEqual "https://www.gov.uk/get-information-about-a-company"
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.RegisteredAddressController.show.toString()
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
    }

    "Verify that the date of incorporation page contains the correct elements when an invalid DateOfIncorporationModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(emptyDateOfIncorporationModel)))
        val result = controller.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.companyDetails.DateOfIncorporation.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.DateOfIncorporation.heading")
      document.body.getElementsByClass("form-hint").text should include(Messages("date.hint.dateOfIncorporation"))
      document.body.getElementById("incorporationDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("incorporationMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("incorporationYear").parent.text shouldBe Messages("common.date.fields.year")
      document.body.getElementById("date-of-incorporation-where-to-find").parent.text should include
      (Messages("page.companyDetails.DateOfIncorporation.location"))
      document.body.getElementById("company-house-db").text() shouldEqual TestHelper.getExternalLinkText(Messages("page.companyDetails.DateOfIncorporation.companiesHouse"))
      document.body.getElementById("company-house-db").attr("href") shouldEqual "https://www.gov.uk/get-information-about-a-company"
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.RegisteredAddressController.show.toString()
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
      document.getElementById("error-summary-display").hasClass("error-summary--show")

    }

  }

}
