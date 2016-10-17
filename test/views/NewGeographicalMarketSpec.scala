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
import builders.SessionBuilder
import common.{Constants, KeystoreKeys}
import config.FrontendAppConfig
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.{NewGeographicalMarketController, routes}
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._

import scala.concurrent.Future

class NewGeographicalMarketSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach {

  val mockS4lConnector = mock[S4LConnector]

  val newGeographicalMarketModel = new NewGeographicalMarketModel(Constants.StandardRadioButtonYesValue)
  val emptyNewGeographicalMarketModel = new NewGeographicalMarketModel("")

  class SetupPage {
    val controller = new NewGeographicalMarketController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val s4lConnector : S4LConnector = mockS4lConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  override def beforeEach() {
    reset(mockS4lConnector)
  }

  "The NewGeographicalMarket Page" +
    "Verify that the correct elements are loaded when navigating from WhatWillUse page" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkNewGeoMarket))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.WhatWillUseForController.show().toString())))
      when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(newGeographicalMarketModel)))
      val result = controller.show.apply(authorisedFakeRequestToPOST("isNewGeographicalMarket" -> Constants.StandardRadioButtonYesValue))
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual routes.WhatWillUseForController.show().toString()
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewGeographicalMarket.heading")
    document.select("#isNewGeographicalMarket-yes").size() shouldBe 1
    document.select("#isNewGeographicalMarket-no").size() shouldBe 1
    document.getElementById("isNewGeographicalMarket-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewGeographicalMarket-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.body.getElementById("external-hint").text shouldBe
      s"${Messages("page.investment.NewGeographicalMarket.hint")} ${Messages("page.investment.NewGeographicalMarket.hint.link")} opens in a new window"
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "The NewGeographicalMarket Page" +
    "Verify that the correct elements are loaded when navigating from UsedInvestmentReasonBefore page" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkNewGeoMarket))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.UsedInvestmentReasonBeforeController.show().toString())))
      when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(newGeographicalMarketModel)))
      val result = controller.show.apply(authorisedFakeRequestToPOST("isNewGeographicalMarket" -> Constants.StandardRadioButtonYesValue))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.UsedInvestmentReasonBeforeController.show().toString()
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewGeographicalMarket.heading")
    document.select("#isNewGeographicalMarket-yes").size() shouldBe 1
    document.select("#isNewGeographicalMarket-no").size() shouldBe 1
    document.getElementById("isNewGeographicalMarket-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewGeographicalMarket-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.body.getElementById("external-hint").text shouldBe
      s"${Messages("page.investment.NewGeographicalMarket.hint")} ${Messages("page.investment.NewGeographicalMarket.hint.link")} opens in a new window"
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "The NewGeographicalMarket Page" +
    "Verify that the correct elements are loaded when navigating from PreviousBeforeDOFCS page" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkNewGeoMarket))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.PreviousBeforeDOFCSController.show().toString())))
      when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(newGeographicalMarketModel)))
      val result = controller.show.apply(authorisedFakeRequestToPOST("isNewGeographicalMarket" -> Constants.StandardRadioButtonYesValue))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.PreviousBeforeDOFCSController.show().toString()
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewGeographicalMarket.heading")
    document.select("#isNewGeographicalMarket-yes").size() shouldBe 1
    document.select("#isNewGeographicalMarket-no").size() shouldBe 1
    document.getElementById("isNewGeographicalMarket-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewGeographicalMarket-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.body.getElementById("external-hint").text shouldBe
      s"${Messages("page.investment.NewGeographicalMarket.hint")} ${Messages("page.investment.NewGeographicalMarket.hint.link")} opens in a new window"
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that NewGeographicalMarket page contains error summary when invalid model is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      // submit the model with no radio selected as a post action
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkNewGeoMarket))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.UsedInvestmentReasonBeforeController.show().toString())))
      when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyNewGeographicalMarketModel)))
      val result = controller.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
  }
}
