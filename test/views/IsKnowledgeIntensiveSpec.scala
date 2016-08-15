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
import common.Constants
import connectors.KeystoreConnector
import controllers.{IsKnowledgeIntensiveController, routes}
import models.IsKnowledgeIntensiveModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class IsKnowledgeIntensiveSpec extends UnitSpec with WithFakeApplication with MockitoSugar{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val isKnowledgeIntensiveModel = new IsKnowledgeIntensiveModel(Constants.StandardRadioButtonYesValue)
  val emptyIsKnowledgeIntensiveModel = new IsKnowledgeIntensiveModel("")

  class SetupPage {

    val controller = new IsKnowledgeIntensiveController{
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "Verify that the isKnowledgeIntensive page contains the correct elements " +
    "when a valid IsKnowledgeIntensiveModel is passed as returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(isKnowledgeIntensiveModel)))
      val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.CommercialSaleController.show().toString()
    document.title() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.title")
    document.getElementById("ki-requirement-definition").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-definition")
    document.getElementById("ki-requirement-one").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-one")
    document.getElementById("ki-requirement-two").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-two")
    document.getElementById("ki-requirement-also").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-also")
    document.getElementById("ki-requirement-three").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-three")
    document.getElementById("ki-requirement-four").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-four")
    document.select("#isKnowledgeIntensive-yes").size() shouldBe 1
    document.select("#isKnowledgeIntensive-yes").size() shouldBe 1
    document.getElementById("isKnowledgeIntensive-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isKnowledgeIntensive-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that isKnowledgeIntensive page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyIsKnowledgeIntensiveModel)))
      val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.CommercialSaleController.show().toString()
    document.title() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.title")
    document.getElementById("ki-requirement-definition").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-definition")
    document.getElementById("ki-requirement-one").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-one")
    document.getElementById("ki-requirement-two").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-two")
    document.getElementById("ki-requirement-also").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-also")
    document.getElementById("ki-requirement-three").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-three")
    document.getElementById("ki-requirement-four").text() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.ki-requirement-four")
    document.select("#isKnowledgeIntensive-yes").size() shouldBe 1
    document.select("#isKnowledgeIntensive-yes").size() shouldBe 1
    document.getElementById("isKnowledgeIntensive-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isKnowledgeIntensive-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that IsKnowledgeIntensive page contains show the error summary when an invalid model (no radio button selection) is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      // submit the model with no radio selected as a post action
      val result = controller.submit.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.companyDetails.IsKnowledgeIntensive.title")

  }
}

