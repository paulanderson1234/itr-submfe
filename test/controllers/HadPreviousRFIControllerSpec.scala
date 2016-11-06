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
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import helpers.ControllerSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class HadPreviousRFIControllerSpec extends ControllerSpec {

  object TestController extends HadPreviousRFIController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "HadPreviousRFIController" should {
    "use the correct keystore connector" in {
      HadPreviousRFIController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      HadPreviousRFIController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      HadPreviousRFIController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(hadPreviousRFIModel: Option[HadPreviousRFIModel] = None, backLink: Option[String] = None,
                 previousSchemes: Option[Vector[PreviousSchemeModel]] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(hadPreviousRFIModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkReviewPreviousSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(previousSchemes))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backLink))
  }

  "Sending a GET request to HadPreviousRFIController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(hadPreviousRFIModelYes), Some(routes.ProposedInvestmentController.show().url))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks(None,Some(routes.ProposedInvestmentController.show().url))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to HadPreviousRFIController when authenticated and NOT enrolled" should {
    "redirect to subscription" in {
      setupMocks(Some(hadPreviousRFIModelYes),Some(routes.ProposedInvestmentController.show().url))
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to HadPreviousRFIController" should {
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

  "Sending a request with no session to HadPreviousRFIController" should {
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

  "Sending a timed-out request to HadPreviousRFIController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the HadPreviousRFIController when authenticated and enrolled" +
    "and there are previous enrolments" should {
    "redirect to review schemes page" in {
      mockEnrolledRequest()
      setupMocks(previousSchemes = Some(previousSchemesValid), backLink = Some(routes.ProposedInvestmentController.show().url))
      val formInput = "hadPreviousRFI" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ReviewPreviousSchemesController.show().url)
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the HadPreviousRFIController when authenticated and enrolled" +
    "and there are no previous enrolments" should {
    "redirect to previous scheme page" in {
      mockEnrolledRequest()
      setupMocks(backLink = Some(routes.ProposedInvestmentController.show().url))
      val formInput = "hadPreviousRFI" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.PreviousSchemeController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the HadPreviousRFIController when authenticated and enrolled" should {
    "redirect to the commercial sale page" in {
      mockEnrolledRequest()
      val formInput = "hadPreviousRFI" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/proposed-investment")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the HadPreviousRFIController when authenticated and enrolled" should {
    "redirect to itself" in {
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkReviewPreviousSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(routes.ProposedInvestmentController.show().url)))

      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Some(routes.ProposedInvestmentController.show().url)))
      mockEnrolledRequest()
      val formInput = "hadPreviousRFI" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }


  "Sending a submission to the HadPreviousRFIController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the HadPreviousRFIController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the HadPreviousRFIController when NOT enrolled" should {
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
