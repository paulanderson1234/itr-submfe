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
import controllers.examples.{DoSubmissionController, routes}
import models.DoSubmissionModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class DosubmissionSpec extends UnitSpec with WithFakeApplication with MockitoSugar{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val doSubmissionModel = new DoSubmissionModel("Yes")
  val emptyDoSubmissionModel = new DoSubmissionModel("")

  class SetupPage {

    val controller = new DoSubmissionController{
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "Verify that AgentView page contains the correct elements when a valid DoSubmissionModel is passed as returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[DoSubmissionModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(doSubmissionModel)))
      val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.DateOfFirstSaleController.show.toString()
    document.title() shouldBe Messages("page.examples.DoSubmission.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.examples.DoSubmission.heading")
    document.getElementById("doSubmissionLegendId").text() shouldBe Messages("page.examples.DoSubmission.question")
    document.select("#doSubmission-yes").size() shouldBe 1
    document.select("#doSubmission-no").size() shouldBe 1
    document.getElementById("doSubmission-yesLabel").text() shouldBe Messages("common.base.yes")
    document.getElementById("doSubmission-noLabel").text() shouldBe Messages("common.base.no")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")

  }

  "Verify that doSubmission page contains the correct elements when an empty model is passed baecause nothing was returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[DoSubmissionModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyDoSubmissionModel)))
      val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.DateOfFirstSaleController.show.toString()
    document.title() shouldBe Messages("page.examples.DoSubmission.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.examples.DoSubmission.heading")
    document.getElementById("doSubmissionLegendId").text() shouldBe Messages("page.examples.DoSubmission.question")
    document.select("#doSubmission-yes").size() shouldBe 1
    document.select("#doSubmission-no").size() shouldBe 1
    document.getElementById("doSubmission-yesLabel").text() shouldBe Messages("common.base.yes")
    document.getElementById("doSubmission-noLabel").text() shouldBe Messages("common.base.no")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that DoSubmission page contains show the error summary when an invalid model (no radio button selection) is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      // subnit the model with no radio slected as a post action
      val result = controller.submit.apply(SessionBuilder.buildRequestWithSession(userId))
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.examples.DoSubmission.title")

  }
}
