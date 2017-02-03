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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.ControllerSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class ProposedInvestmentControllerSpec extends ControllerSpec {

  object TestController extends ProposedInvestmentController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val proposedInvestment = ProposedInvestmentModel(12345)

  "ProposedInvestmentController" should {
    "use the correct keystore connector" in {
      ProposedInvestmentController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      ProposedInvestmentController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      ProposedInvestmentController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct submission connector" in {
      ProposedInvestmentController.submissionConnector shouldBe SubmissionConnector
    }
    "use the correct application config" in {
      ProposedInvestmentController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to ProposedInvestmentController when authenticated and enrolled" should {

    "return a 200 when something is fetched from keystore" in {
      when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(proposedInvestment)))
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.HadPreviousRFIController.show().url)))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.HadPreviousRFIController.show().url)))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

  }

  "Sending a GET request to ProposedInvestmentController without a valid backlink from keystore when authenticated and enrolled" should {
    "redirect to the beginning of the flow" in {
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }
  }

  "Sending a GET request to ProposedInvestmentController when authenticated and NOT enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(TestController.applicationConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to ProposedInvestmentController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${TestController.applicationConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(TestController.applicationConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to NatureOfBusinessController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${TestController.applicationConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(TestController.applicationConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to NatureOfBusinessController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController" should {
    "redirect to the confirm contact details page" in {
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ConfirmContactDetailsController.show().url)
        }
      )
    }
  }


  "Sending an invalid form submission with validation errors to the ProposedInvestmentController" should {
    "redirect to itself" in {
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.HadPreviousRFIController.show().url)))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }


  "Sending a submission to the NatureOfBusinessController when not authenticated" should {

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
  }

  "Sending a submission to the NatureOfBusinessController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the NatureOfBusinessController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(TestController.applicationConfig.subscriptionUrl)
        }
      )
    }
  }
}

