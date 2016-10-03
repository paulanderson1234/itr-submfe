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
import controllers.{CommercialSaleController, routes}
import controllers.helpers.FakeRequestHelper
import models.CommercialSaleModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class CommercialSaleSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockKeystoreConnector = mock[KeystoreConnector]

  val commercialSaleModelValidNo = new CommercialSaleModel(Constants.StandardRadioButtonNoValue, None, None, None)
  val commercialSaleModelValidYes = new CommercialSaleModel(Constants.StandardRadioButtonYesValue, Some(10), Some(25), Some(2015))
  val commercialSaleModelInvalidYes = new CommercialSaleModel(Constants.StandardRadioButtonYesValue, None, Some(25), Some(2015))
  val emptyCommercialSaleModel = new CommercialSaleModel("", None, None, None)

  class SetupPage {

    val controller = new CommercialSaleController {
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  "The Contact Details page" should {

    "Verify that the commercial sale page contains the correct elements when a valid 'Yes' CommercialSaleModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(commercialSaleModelValidYes)))
        val result = controller.show.apply((authorisedFakeRequest.withFormUrlEncodedBody(
          "hasCommercialSale" -> Constants.StandardRadioButtonYesValue,
          "commercialSaleDay" -> "10",
          "commercialSaleMonth" -> "25",
          "commercialSaleYear" -> "2015"
        )))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.companyDetails.CommercialSale.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.CommercialSale.heading")
      document.getElementById("form-hint-id").text() shouldBe Messages("common.date.hint.example")
      document.getElementById("question-text-id").text() shouldBe Messages("page.companyDetails.CommercialSale.question.hint")
      document.getElementById("question-text-id").hasClass("h2-heading")
      document.getElementById("question-date-text-legend-id").hasClass("visuallyhidden")
      document.getElementById("hasCommercialSale-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasCommercialSale-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.NatureOfBusinessController.show.toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
    }


    "Verify that the commercial sale page contains the correct elements when a valid 'No' CommercialSaleModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(commercialSaleModelValidNo)))
        val result = controller.show.apply((authorisedFakeRequest.withFormUrlEncodedBody(
          "hasCommercialSale" -> Constants.StandardRadioButtonNoValue,
          "commercialSaleDay" -> "",
          "commercialSaleMonth" -> "",
          "commercialSaleYear" -> ""
        )))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.companyDetails.CommercialSale.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.CommercialSale.heading")
      document.getElementById("form-hint-id").text() shouldBe Messages("common.date.hint.example")
      document.getElementById("question-text-id").text() shouldBe Messages("page.companyDetails.CommercialSale.question.hint")
      document.getElementById("question-text-id").hasClass("h2-heading")
      document.getElementById("question-date-text-legend-id").hasClass("visuallyhidden")
      document.getElementById("hasCommercialSale-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasCommercialSale-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.NatureOfBusinessController.show.toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
    }

    "Verify that the commercial sale page contains the correct elements when an invalid CommercialSaleModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(emptyCommercialSaleModel)))
        val result = controller.submit.apply((authorisedFakeRequest))
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.CommercialSale.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.CommercialSale.heading")
      document.getElementById("form-hint-id").text() shouldBe Messages("common.date.hint.example")
      document.getElementById("question-text-id").text() shouldBe Messages("page.companyDetails.CommercialSale.question.hint")
      document.getElementById("question-text-id").hasClass("h2-heading")
      document.getElementById("question-date-text-legend-id").hasClass("visuallyhidden")
      document.getElementById("hasCommercialSale-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasCommercialSale-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.NatureOfBusinessController.show.toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

    "Verify that the commercial sale page contains the correct elements when an invalid CommercialSaleYesModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(commercialSaleModelInvalidYes)))
        val result = controller.submit.apply((authorisedFakeRequest))
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.CommercialSale.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.CommercialSale.heading")
      document.getElementById("form-hint-id").text() shouldBe Messages("common.date.hint.example")
      document.getElementById("question-text-id").text() shouldBe Messages("page.companyDetails.CommercialSale.question.hint")
      document.getElementById("question-text-id").hasClass("h2-heading")
      document.getElementById("question-date-text-legend-id").hasClass("visuallyhidden")
      document.getElementById("hasCommercialSale-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hasCommercialSale-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.NatureOfBusinessController.show.toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }
}
