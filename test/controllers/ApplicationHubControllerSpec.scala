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
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.{RegistrationDetailsService, SubscriptionService}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Authority
import uk.gov.hmrc.play.http.HttpResponse
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
    override lazy val subscriptionService = mockSubscriptionService
    override lazy val registrationDetailsService = mockRegistrationDetailsService
  }

  def setupMocks(bool: Option[Boolean]): Unit = {
    when(mockRegistrationDetailsService.getRegistrationDetails(Matchers.any())(Matchers.any(),Matchers.any(), Matchers.any())).
      thenReturn(Future.successful(Some(registrationDetailsModel)))
    when(mockSubscriptionService.getSubscriptionContactDetails(Matchers.any())(Matchers.any(),Matchers.any())).
      thenReturn(Future.successful(Some(contactDetailsModel)))
    when(mockS4lConnector.fetchAndGetFormData[Boolean](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(bool))
  }

  def setupMocksNotAvailable(): Unit = {
    when(mockRegistrationDetailsService.getRegistrationDetails(Matchers.any())(Matchers.any(),Matchers.any(), Matchers.any())).
      thenReturn(Future.successful(None))
    when(mockSubscriptionService.getSubscriptionContactDetails(Matchers.any())(Matchers.any(),Matchers.any())).
      thenReturn(Future.successful(None))
  }

  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(true)))


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
    "use the correct registration service" in {
      ApplicationHubController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct service service" in {
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
          contentAsString(result) contains ApplicationHubNew
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
          contentAsString(result) contains ApplicationHubNew
        }
      )
    }

    "return a 500 when an ApplicationHubModel cannot be composed" in {
      setupMocksNotAvailable()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
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

  "Posting to the 'create new application' button on the ApplicationHubController when authenticated and enrolled" should {
    "redirect to 'your company need' page if table is not empty" in {
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.newApplication)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/your-company-need")
        }
      )
    }
  }

  "Sending a POST request to ApplicationHubController delete method when authenticated and enrolled" should {
    "redirect to itself and delete the application currently in progress" in {
      when(mockS4lConnector.clearCache()(Matchers.any(),Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.delete)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/hub")
        }
      )
    }
  }

  "Posting to 'newApplication' when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(TestController.newApplication)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(TestController.newApplication)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Posting to 'newApplication' in the ApplicationHubController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.newApplication)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Posting to 'newApplication' in the ApplicationHubController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest()
      submitWithSessionAndAuth(TestController.newApplication)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Posting to 'delete' when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(TestController.delete)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(TestController.delete)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Posting to 'delete' in the ApplicationHubController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.delete)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Posting to 'delete' in the ApplicationHubController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest()
      submitWithSessionAndAuth(TestController.delete)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }
}
