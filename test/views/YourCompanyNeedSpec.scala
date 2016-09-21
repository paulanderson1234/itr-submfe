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

import auth.MockAuthConnector
import builders.SessionBuilder
import config.FrontendAppConfig
import connectors.KeystoreConnector
import controllers.helpers.FakeRequestHelper
import controllers.{YourCompanyNeedController, routes}
import models.YourCompanyNeedModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class YourCompanyNeedSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val yourCompanyNeedModel = new YourCompanyNeedModel("AA")
  val emptyYourCompanyNeedModel = new YourCompanyNeedModel("")

  class SetupPage {

    val controller = new YourCompanyNeedController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "Verify that the yourCompanyNeed page contains the correct elements " +
    "when a valid YourCompanyNeedModel is passed as returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(yourCompanyNeedModel)))
      val result = controller.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.IntroductionController.show.toString()
    document.title() shouldBe Messages("page.introduction.YourCompanyNeed.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.introduction.YourCompanyNeed.heading")
    document.select("#needAAorCS-aa").size() shouldBe 1
    document.select("#needAAorCS-cs").size() shouldBe 1
    document.getElementById("needAAorCS-aaLabel").text() shouldBe Messages("page.introduction.YourCompanyNeed.advancedAssurance")
    document.getElementById("needAAorCS-csLabel").text() shouldBe Messages("page.introduction.YourCompanyNeed.complianceStatement")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")

  }

  "Verify that yourCompanyNeed page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyYourCompanyNeedModel)))
      val result = controller.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.IntroductionController.show.toString()
    document.title() shouldBe Messages("page.introduction.YourCompanyNeed.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.introduction.YourCompanyNeed.heading")
    document.select("#needAAorCS-aa").size() shouldBe 1
    document.select("#needAAorCS-cs").size() shouldBe 1
    document.getElementById("needAAorCS-aaLabel").text() shouldBe Messages("page.introduction.YourCompanyNeed.advancedAssurance")
    document.getElementById("needAAorCS-csLabel").text() shouldBe Messages("page.introduction.YourCompanyNeed.complianceStatement")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that YourCompanyNeed page contains show the error summary when an invalid model (no radio button selection) is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      // submit the model with no radio slected as a post action
      val result = controller.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.introduction.YourCompanyNeed.title")

  }
}
