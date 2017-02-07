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
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.ControllerSpec
import play.api.test.Helpers._
import views.helpers.CheckAnswersSpec

class CheckAnswersControllerSpec extends ControllerSpec with CheckAnswersSpec {
  
  object TestController extends CheckAnswersController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "CheckAnswersController" should {
    "use the correct auth connector" in {
      CheckAnswersController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      CheckAnswersController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      CheckAnswersController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to CheckAnswersController with a populated set of models when authenticated and enrolled" should {
    "return a 200 when the page is loaded" in {
      previousRFISetup(Some(hadPreviousRFIModelYes))
      seisInvestmentSetup(Some(proposedInvestmentModel), Some(subsidiariesSpendingInvestmentModelNo), Some(subsidiariesNinetyOwnedModelNo))
      contactDetailsSetup(Some(contactDetailsModel))
      contactAddressSetup(Some(contactAddressModel))
      seisCompanyDetailsSetup(Some(registeredAddressModel), Some(dateOfIncorporationModel),
        Some(natureOfBusinessModel), Some(subsidiariesModelYes), Some(tradeStartDateModelYes))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show(envelopeId))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to CheckAnswersController with an empty set of models when authenticated and enrolled" should {
    "return a 200 when the page is loaded" in {
      previousRFISetup()
      seisInvestmentSetup()
      contactDetailsSetup()
      seisCompanyDetailsSetup()
      contactAddressSetup()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show(envelopeId))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request (with envelope id None) to CheckAnswersController with an empty set of models when authenticated and enrolled" should {
    "return a 200 when the page is loaded" in {
      previousRFISetup()
      seisInvestmentSetup()
      contactDetailsSetup()
      seisCompanyDetailsSetup()
      contactAddressSetup()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show(None))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request (with empty envelope id) to CheckAnswersController with an empty set of models when authenticated and enrolled" should {
    "return a 200 when the page is loaded" in {
      previousRFISetup()
      seisInvestmentSetup()
      contactDetailsSetup()
      seisCompanyDetailsSetup()
      contactAddressSetup()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show(Some("")))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending an Authenticated and NOT Enrolled GET request with a session to CheckAnswersControllerTest" should {
    "redirect to the TAVC Subscription Service" in {
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show(envelopeId))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(TestController.applicationConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Authenticated and NOT Enrolled GET request (envelopeId is None) with a session to CheckAnswersControllerTest" should {
    "redirect to the TAVC Subscription Service" in {
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show(None))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(TestController.applicationConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to CheckAnswersController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(TestController.show(envelopeId))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${TestController.applicationConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(TestController.applicationConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending an Unauthenticated request (envelopeId is None) with a session to CheckAnswersController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(TestController.show(None))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${TestController.applicationConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(TestController.applicationConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to CheckAnswersController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(TestController.show(envelopeId))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${TestController.applicationConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(TestController.applicationConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request (envelopeId is None) with no session to CheckAnswersController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(TestController.show(None))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${TestController.applicationConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(TestController.applicationConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to CheckAnswersController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show(envelopeId))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the CheckAnswersController" should {

    "redirect to the acknowledgement page when authenticated and enrolled" in {
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.AcknowledgementController.show().url)
        }
      )
    }

    "redirect to the subscription service when authenticated and NOT enrolled" in {
      mockNotEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(TestController.applicationConfig.subscriptionUrl)
        }
      )
    }

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${TestController.applicationConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(TestController.applicationConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${TestController.applicationConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(TestController.applicationConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }
}
