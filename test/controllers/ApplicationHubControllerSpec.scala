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

package controllers

import java.net.URLEncoder

import auth.{MockAuthConnector, MockConfig}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.ControllerSpec
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.html.hubPartials.{ApplicationHubExisting, ApplicationHubNew}

import scala.concurrent.Future

/**
  * Created by jade on 01/11/16.
  */
class ApplicationHubControllerSpec extends ControllerSpec{


  object TestController extends ApplicationHubController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(bool: Option[Boolean]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Boolean](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(bool))
  }

  "ApplicationHubController" should {
    "use the correct auth connector" in {
      ApplicationHubController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      ApplicationHubController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      ApplicationHubController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to ApplicationHubController when authenticated and enrolled" should {
    "return a 200 when true is fetched from keystore" in {
      setupMocks(Some(true))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
          contentAsString(result) contains ApplicationHubExisting
          //document.body()
        }
      )
    }

    "return a 200 when false is fetched from keystore" in {
      setupMocks(Some(false))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
          val document = Jsoup.parse(contentAsString(result))
          document.getElementById("create-new-application").text() shouldBe Messages("page.introduction.hub.button")
        }
      )
    }

    "return a 200 when nothing is fetched from keystore" in {
      setupMocks(None)
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
          val document = Jsoup.parse(contentAsString(result))
          document.getElementById("create-new-application").text() shouldBe Messages("page.introduction.hub.button")
        }
      )
    }
  }

  "Sending a GET request to ApplicationHubController when authenticated and NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to ApplicationHubController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to ApplicationHubController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to ApplicationHubController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }
}
