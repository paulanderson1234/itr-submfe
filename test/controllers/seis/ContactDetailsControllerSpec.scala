/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.seis

import java.net.URLEncoder

import auth.{MockAuthConnector, MockConfig}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.ControllerSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class ContactDetailsControllerSpec extends ControllerSpec {

  object ContactDetailsControllerTest extends ContactDetailsController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "ContactDetailsController" should {
    "use the correct keystore connector" in {
      ContactDetailsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      ContactDetailsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      ContactDetailsController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(contactDetailsModel: Option[ContactDetailsModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(contactDetailsModel))

  "Sending a GET request to ContactDetailsController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(contactDetailsModel))
      mockEnrolledRequest()
      showWithSessionAndAuth(ContactDetailsControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks()
      mockEnrolledRequest()
      showWithSessionAndAuth(ContactDetailsControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to ContactDetailsController when authenticated and NOT enrolled" should {
    "redirect to the Subscription Service" in {
      setupMocks(Some(contactDetailsModel))
      mockNotEnrolledRequest()
      showWithSessionAndAuth(ContactDetailsControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to ContactDetailsController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(ContactDetailsControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to ContactDetailsController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(ContactDetailsControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to ContactDetailsController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(ContactDetailsControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ContactDetailsController when authenticated and enrolled" should {
    "redirect to the Confirm Correspondence Address Controller page" in {
      mockEnrolledRequest()
      val formInput = Seq(
        "forename" -> "first",
        "surname" -> "last",
        "telephoneNumber" -> "01385 236846",
        "email" -> "test@test.com"
      )
      submitWithSessionAndAuth(ContactDetailsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/seis/confirm-correspondence-address")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ContactDetailsController when authenticated and enrolled" should {
    "redirect with a bad request" in {
      mockEnrolledRequest()
      val formInput = Seq(
        "forename" -> "first",
        "surname" -> "",
        "telephoneNumber" -> "",
        "mobileNumber" -> "",
        "email" -> "test@test.com")
      submitWithSessionAndAuth(ContactDetailsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a valid form submit to the ContactDetailsController when authenticated and NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest()
      val formInput = Seq(
        "forename" -> "first",
        "surname" -> "last",
        "telephoneNumber" -> "01385 236846",
        "email" -> "test@test.com"
      )
      submitWithSessionAndAuth(ContactDetailsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending a submission to the ContactDetailsController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(ContactDetailsControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(ContactDetailsControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the ContactDetailsController when a timeout has occurred" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(ContactDetailsControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

}
