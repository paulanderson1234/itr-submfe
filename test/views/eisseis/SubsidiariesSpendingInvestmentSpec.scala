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

package views.eisseis

import auth.{MockConfig, MockAuthConnector}
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.eisseis.SubsidiariesSpendingInvestmentController
import controllers.routes
import models.SubsidiariesSpendingInvestmentModel
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class SubsidiariesSpendingInvestmentSpec extends ViewSpec {
  
  object TestController extends SubsidiariesSpendingInvestmentController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(subsidiariesSpendingInvestmentModel: Option[SubsidiariesSpendingInvestmentModel] = None, backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesSpendingInvestmentModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisSchemeTypesModel))
  }
  
  "The SubsidiariesSpendingInvestment Page" +
    "Verify that the correct elements are loaded navigating from WhatWillUseFor page" in new Setup {
    val document: Document = {
      setupMocks(Some(subsidiariesSpendingInvestmentModelYes), Some(controllers.eisseis.routes.ProposedInvestmentController.show().url))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ProposedInvestmentController.show().url
    document.title() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.heading")
    document.select("#subSpendingInvestment-yes").size() shouldBe 1
    document.select("#subSpendingInvestment-no").size() shouldBe 1
    document.getElementById("subSpendingInvestment-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("subSpendingInvestment-noLabel").text() shouldBe Messages("common.radioNoLabel")
  }

  "Verify that the correct elements are loaded when navigating from PreviousBeforeDOFCS page" in new Setup {
    val document: Document = {
      setupMocks(Some(subsidiariesSpendingInvestmentModelYes), Some(controllers.eisseis.routes.PreviousBeforeDOFCSController.show().url))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.PreviousBeforeDOFCSController.show().url
    document.title() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.heading")
    document.select("#subSpendingInvestment-yes").size() shouldBe 1
    document.select("#subSpendingInvestment-no").size() shouldBe 1
    document.getElementById("subSpendingInvestment-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("subSpendingInvestment-noLabel").text() shouldBe Messages("common.radioNoLabel")
  }

  "Verify that the correct elements are loaded when navigating from NewProduct page" in new Setup {
    val document: Document = {
      setupMocks(Some(subsidiariesSpendingInvestmentModelYes), Some(controllers.eisseis.routes.NewProductController.show().url))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.NewProductController.show().url
    document.title() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.heading")
    document.select("#subSpendingInvestment-yes").size() shouldBe 1
    document.select("#subSpendingInvestment-no").size() shouldBe 1
    document.getElementById("subSpendingInvestment-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("subSpendingInvestment-noLabel").text() shouldBe Messages("common.radioNoLabel")
  }

  "Verify that SubsidiariesSpendingInvestment page contains error summary when invalid model is submitted" in new Setup {
    val document : Document = {
      setupMocks(backLink = Some(controllers.eisseis.routes.ProposedInvestmentController.show().url))
      // submit the model with no radio selected as a post action
      val result = TestController.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.title")
  }
}
