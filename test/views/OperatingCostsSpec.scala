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
import controllers.{IsKnowledgeIntensiveController, OperatingCostsController, routes}
import models.{IsKnowledgeIntensiveModel, OperatingCostsModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class OperatingCostsSpec extends UnitSpec with WithFakeApplication with MockitoSugar{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val operatingCostsModel = OperatingCostsModel("750000", "800000", "934000", "231000", "340000", "344000")
  val emptyOperatingCostsModel = new OperatingCostsModel("", "", "", "", "", "")

  class SetupPage {

    val controller = new OperatingCostsController{
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "Verify that the OperatingCosts page contains the correct elements " +
    "when a valid OperatingCostsModel is passed as returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(operatingCostsModel)))
      val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show().toString()
    document.title() shouldBe Messages("page.companyDetails.OperatingCosts.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.OperatingCosts.heading")
    document.getElementById("operating-costs-hint-one").text() shouldBe Messages("page.companyDetails.OperatingCosts.hint.one")
    document.getElementById("operating-costs-hint-two").text() shouldBe Messages("page.companyDetails.OperatingCosts.hint.two")
    document.getElementById("col-heading-one").hasClass("visuallyhidden")
    document.getElementById("col-heading-one").text() shouldBe Messages("page.companyDetails.OperatingCosts.col.heading.one")
    document.getElementById("col-heading-two").text() shouldBe Messages("page.companyDetails.OperatingCosts.col.heading.two")
    document.getElementById("col-heading-three").text() shouldBe Messages("page.companyDetails.OperatingCosts.col.heading.three")
    document.getElementById("row-heading-one").text() shouldBe Messages("page.companyDetails.OperatingCosts.row.heading.one")
    document.getElementById("row-heading-two").text() shouldBe Messages("page.companyDetails.OperatingCosts.row.heading.two")
    document.getElementById("row-heading-three").text() shouldBe Messages("page.companyDetails.OperatingCosts.row.heading.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")

  }

  "Verify that OperatingCosts page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyOperatingCostsModel)))
      val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show().toString()
    document.title() shouldBe Messages("page.companyDetails.OperatingCosts.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.OperatingCosts.heading")
    document.getElementById("operating-costs-hint-one").text() shouldBe Messages("page.companyDetails.OperatingCosts.hint.one")
    document.getElementById("operating-costs-hint-two").text() shouldBe Messages("page.companyDetails.OperatingCosts.hint.two")
    document.getElementById("col-heading-one").hasClass("visuallyhidden")
    document.getElementById("col-heading-one").text() shouldBe Messages("page.companyDetails.OperatingCosts.col.heading.one")
    document.getElementById("col-heading-two").text() shouldBe Messages("page.companyDetails.OperatingCosts.col.heading.two")
    document.getElementById("col-heading-three").text() shouldBe Messages("page.companyDetails.OperatingCosts.col.heading.three")
    document.getElementById("row-heading-one").text() shouldBe Messages("page.companyDetails.OperatingCosts.row.heading.one")
    document.getElementById("row-heading-two").text() shouldBe Messages("page.companyDetails.OperatingCosts.row.heading.two")
    document.getElementById("row-heading-three").text() shouldBe Messages("page.companyDetails.OperatingCosts.row.heading.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that IsKnowledgeIntensive page contains show the error summary when an invalid model (no radio button selection) is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      // submit the model with no radio slected as a post action
      val result = controller.submit.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.companyDetails.OperatingCosts.title")

  }
}

