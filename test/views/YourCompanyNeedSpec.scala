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
import controllers.{YourCompanyNeedController, routes}
import models.YourCompanyNeedModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewTestSpec

import scala.concurrent.Future

class YourCompanyNeedSpec extends ViewTestSpec {

  object TestController extends YourCompanyNeedController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(yourCompanyNeedModel: Option[YourCompanyNeedModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(yourCompanyNeedModel))
  }

  "Verify that the yourCompanyNeed page contains the correct elements " +
    "when a valid YourCompanyNeedModel is passed as returned from keystore" in new Setup {
    val document : Document = {
      setupMocks(Some(yourCompanyNeedModel))
      val result = TestController.show().apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual routes.IntroductionController.show().url
    document.title() shouldBe Messages("page.introduction.YourCompanyNeed.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.introduction.YourCompanyNeed.heading")
    document.select("#needAAorCS-aa").size() shouldBe 1
    document.select("#needAAorCS-cs").size() shouldBe 1
    document.getElementById("needAAorCS-aaLabel").text() shouldBe Messages("page.introduction.YourCompanyNeed.advancedAssurance")
    document.getElementById("needAAorCS-csLabel").text() shouldBe Messages("page.introduction.YourCompanyNeed.complianceStatement")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that yourCompanyNeed page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new Setup {
    val document : Document = {
      setupMocks()
      val result = TestController.show().apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual routes.IntroductionController.show().url
    document.title() shouldBe Messages("page.introduction.YourCompanyNeed.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.introduction.YourCompanyNeed.heading")
    document.select("#needAAorCS-aa").size() shouldBe 1
    document.select("#needAAorCS-cs").size() shouldBe 1
    document.getElementById("needAAorCS-aaLabel").text() shouldBe Messages("page.introduction.YourCompanyNeed.advancedAssurance")
    document.getElementById("needAAorCS-csLabel").text() shouldBe Messages("page.introduction.YourCompanyNeed.complianceStatement")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that YourCompanyNeed page contains show the error summary when an invalid model (no radio button selection) is submitted" in new Setup {
    val document : Document = {
      // submit the model with no radio slected as a post action
      val result = TestController.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.introduction.YourCompanyNeed.title")
  }
}
