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
import common.{Constants, KeystoreKeys}
import config.FrontendAppConfig
import controllers.eis.CommercialSaleController
import models.CommercialSaleModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class CommercialSaleSpec extends ViewSpec {

  val commercialSaleModelInvalidYes = new CommercialSaleModel(Constants.StandardRadioButtonYesValue, None, Some(25), Some(2015))

  object TestController extends CommercialSaleController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(commercialSaleModel: Option[CommercialSaleModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(commercialSaleModel))

  "The Contact Details page" should {

    "Verify that the commercial sale page contains the correct elements when a valid 'Yes' CommercialSaleModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(commercialSaleModelYes))
        val result = TestController.show.apply(authorisedFakeRequest)
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
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.DateOfIncorporationController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
    }


    "Verify that the commercial sale page contains the correct elements when a valid 'No' CommercialSaleModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(commercialSaleModelNo))
        val result = TestController.show.apply(authorisedFakeRequest)
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
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.DateOfIncorporationController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
    }

    "Verify that the commercial sale page contains the correct elements when an invalid CommercialSaleModel is passed" in new Setup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
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
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.DateOfIncorporationController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

    "Verify that the commercial sale page contains the correct elements when an invalid CommercialSaleYesModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(commercialSaleModelInvalidYes))
        val result = TestController.submit.apply(authorisedFakeRequest)
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
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.DateOfIncorporationController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }
}
