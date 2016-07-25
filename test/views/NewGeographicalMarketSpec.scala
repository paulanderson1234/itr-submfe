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
import connectors.KeystoreConnector
import controllers.{NewGeographicalMarketController, routes}
import models.NewGeographicalMarketModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class NewGeographicalMarketSpec extends UnitSpec with WithFakeApplication with MockitoSugar{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val isNewGeographicalMarketModel = new NewGeographicalMarketModel("Yes")
  val emptyNewGeographicalMarketModel = new NewGeographicalMarketModel("")

  class SetupPage {

    val controller = new NewGeographicalMarketController{
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "Verify that the NewGeographicalMarket page contains the correct elements " +
    "when a valid NewGeographicalMarketModel is passed as returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(isNewGeographicalMarketModel)))
      val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.ProposedInvestmentController.show().toString()
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewGeographicalMarket.heading")
    document.select("#isNewGeographicalMarket-yes").size() shouldBe 1
    document.select("#isNewGeographicalMarket-no").size() shouldBe 1
    document.getElementById("isNewGeographicalMarket-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewGeographicalMarket-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("bullet-heading-hint").text() shouldBe Messages("page.investment.NewGeographicalMarket.hint")
    document.getElementById("bullet-one").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.one")
    document.getElementById("bullet-two").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.two")
    document.getElementById("bullet-three").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.three")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that NewGeographicalMarket page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyNewGeographicalMarketModel)))
      val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.ProposedInvestmentController.show().toString()
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewGeographicalMarket.heading")
    document.select("#isNewGeographicalMarket-yes").size() shouldBe 1
    document.select("#isNewGeographicalMarket-no").size() shouldBe 1
    document.getElementById("isNewGeographicalMarket-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewGeographicalMarket-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("bullet-heading-hint").text() shouldBe Messages("page.investment.NewGeographicalMarket.hint")
    document.getElementById("bullet-one").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.one")
    document.getElementById("bullet-two").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.two")
    document.getElementById("bullet-three").text() shouldBe Messages("page.investment.NewGeographicalMarket.bullet.three")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that NewGeographicalMarket page contains error summary when invalid model is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      // submit the model with no radio slected as a post action
      val result = controller.submit.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.investment.NewGeographicalMarket.title")
  }
}
