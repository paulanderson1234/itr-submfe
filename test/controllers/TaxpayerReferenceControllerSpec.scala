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
import helpers.ControllerSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class TaxpayerReferenceControllerSpec extends ControllerSpec {

  object TestController extends TaxpayerReferenceController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(taxpayerReferenceModel: Option[TaxpayerReferenceModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(taxpayerReferenceModel))

  "TaxpayerReferenceController" should {
    "use the correct keystore connector" in {
      TaxpayerReferenceController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      TaxpayerReferenceController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      TaxpayerReferenceController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to TaxpayerReferenceController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(taxpayerReferenceModel))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid form submit to the TaxpayerReferenceController when authenticated and enrolled" should {
    "redirect to the  company's registered address page" in {
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit, "utr" -> "1234567891")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/registered-address")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the TaxpayerReferenceController" should {
    "redirect with a bad request" in {
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit, "utr" -> "fff")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a request with no session to TaxpayerReferenceController" should {
    "return a 303" in {
      status(TestController.show(fakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(TestController.show(fakeRequest)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending an Unauthenticated request with a session to TaxpayerReferenceController" should {
    "return a 303" in {
      status(TestController.show(fakeRequestWithSession)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(TestController.show(fakeRequestWithSession)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending a timed-out request to TaxpayerReferenceController" should {

    "return a 303 in" in {
      status(TestController.show(timedOutFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to timeout page" in {
      redirectLocation(TestController.show(timedOutFakeRequest)) shouldBe Some(routes.TimeoutController.timeout().url)
    }
  }

  "Sending a request to TaxpayerReferenceController when NOT enrolled" should {

    "return a 303 in" in {
      mockNotEnrolledRequest()
      status(TestController.show(authorisedFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to Subscription Service" in {
      mockNotEnrolledRequest()
      redirectLocation(TestController.show(authorisedFakeRequest)) shouldBe Some(FrontendAppConfig.subscriptionUrl)
    }
  }

  "Sending a submission to the TaxpayerReferenceController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the TaxpayerReferenceController with no session" should {

    "redirect to the GG login page with no session" in {
      submitWithoutSession(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the TaxpayerReferenceController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the TaxpayerReferenceController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

}
