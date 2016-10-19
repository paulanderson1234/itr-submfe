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

import auth.MockAuthConnector
import config.FrontendAppConfig
import controllers.{TaxpayerReferenceController, routes}
import models.TaxpayerReferenceModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class TaxpayerReferenceSpec extends ViewSpec {

  object TestController extends TaxpayerReferenceController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(taxpayerReferenceModel: Option[TaxpayerReferenceModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(taxpayerReferenceModel))

  "The Taxpayer Reference page" should {

    "Verify that the taxpayer reference page contains the correct elements when a valid TaxpayerReferenceModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(taxpayerReferenceModel))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.utr.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.utr.heading")
      document.getElementById("help").text() shouldBe Messages("page.companyDetails.utr.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.companyDetails.utr.help.text")
      document.getElementById("label-utr").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-utr").select(".visuallyhidden").text() shouldBe Messages("page.companyDetails.utr.heading")
      document.getElementById("label-utr-hint").text() shouldBe Messages("page.companyDetails.utr.question.hint")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.WhatWeAskYouController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
    }

    "Verify that the taxpayer reference page contains the correct elements when an invalid TaxpayerReferenceModel is passed" in new Setup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.utr.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.utr.heading")
      document.getElementById("help").text() shouldBe Messages("page.companyDetails.utr.help.link")
      document.getElementById("help-text").text() shouldBe Messages("page.companyDetails.utr.help.text")
      document.getElementById("label-utr").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-utr").select(".visuallyhidden").text() shouldBe Messages("page.companyDetails.utr.heading")
      document.getElementById("label-utr-hint").text() shouldBe Messages("page.companyDetails.utr.question.hint")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.WhatWeAskYouController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }

}
