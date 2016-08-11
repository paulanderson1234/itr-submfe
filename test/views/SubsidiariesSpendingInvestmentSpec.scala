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

import builders.SessionBuilder
import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
import controllers.{SubsidiariesSpendingInvestmentController, routes}
import controllers.helpers.FakeRequestHelper
import models.{PreviousBeforeDOFCSModel, WhatWillUseForModel, NewProductModel, SubsidiariesSpendingInvestmentModel}
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

class SubsidiariesSpendingInvestmentSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  val subsidiariesSpendingInvestmentModel = new SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonYesValue)
  val emptySubsidiariesSpendingInvestmentModel = new SubsidiariesSpendingInvestmentModel("")

  class SetupPage {
    val controller = new SubsidiariesSpendingInvestmentController{
      val keyStoreConnector : KeystoreConnector = mockKeyStoreConnector
    }
  }

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }


  "The SubsidiariesSpendingInvestment Page" +
    "Verify that the correct elements are loaded navigating from WhatWillUseFor page" in new SetupPage{
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(routes.WhatWillUseForController.show().toString())))
        when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(subsidiariesSpendingInvestmentModel)))
        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "subSpendingInvestment" -> Constants.StandardRadioButtonYesValue
        ))
        Jsoup.parse(contentAsString(result))
      }

      document.body.getElementById("back-link").attr("href") shouldEqual routes.WhatWillUseForController.show().toString()
      document.title() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.heading")
      document.select("#subSpendingInvestment-yes").size() shouldBe 1
      document.select("#subSpendingInvestment-no").size() shouldBe 1
      document.getElementById("subSpendingInvestment-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subSpendingInvestment-noLabel").text() shouldBe Messages("common.radioNoLabel")
    }

  "Verify that the correct elements are loaded when navigating from PreviousBeforeDOFCS page" in new SetupPage{
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.PreviousBeforeDOFCSController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
        "subSpendingInvestment" -> Constants.StandardRadioButtonYesValue
      ))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.PreviousBeforeDOFCSController.show().toString()
    document.title() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.heading")
    document.select("#subSpendingInvestment-yes").size() shouldBe 1
    document.select("#subSpendingInvestment-no").size() shouldBe 1
    document.getElementById("subSpendingInvestment-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("subSpendingInvestment-noLabel").text() shouldBe Messages("common.radioNoLabel")
  }

  "Verify that the correct elements are loaded when navigating from NewProduct page" in new SetupPage {
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.NewProductController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(subsidiariesSpendingInvestmentModel)))
      val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
        "subSpendingInvestment" -> Constants.StandardRadioButtonYesValue
      ))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.NewProductController.show().toString()
    document.title() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.heading")
    document.select("#subSpendingInvestment-yes").size() shouldBe 1
    document.select("#subSpendingInvestment-no").size() shouldBe 1
    document.getElementById("subSpendingInvestment-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("subSpendingInvestment-noLabel").text() shouldBe Messages("common.radioNoLabel")
  }

  "Verify that SubsidiariesSpendingInvestment page contains error summary when invalid model is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      // submit the model with no radio selected as a post action
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.WhatWillUseForController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(emptySubsidiariesSpendingInvestmentModel)))
      val result = controller.submit.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.investment.SubsidiariesSpendingInvestment.title")
  }
}
