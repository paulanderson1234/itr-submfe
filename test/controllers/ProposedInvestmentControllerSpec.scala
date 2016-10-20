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
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import helpers.ControllerSpec

import scala.concurrent.Future

class ProposedInvestmentControllerSpec extends ControllerSpec {

  object TestController extends ProposedInvestmentController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val model1 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeEisValue, 2356, None, None, Some(4), Some(12), Some(2009), Some(1))
  val model2 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeSeisValue, 2356, Some(666), None, Some(4), Some(12), Some(2010), Some(3))
  val model3 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 2356, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model4 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 19999999, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model5 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 1, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model6 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 11999999, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model7 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 15000000, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))

  val previousSchemeTrueKIVectorList = Vector(model1, model2, model3)
  val previousSchemeOverTrueKIVectorList = Vector(model4, model5, model5)
  val previousSchemeFalseKIVectorList = Vector(model1, model2, model3)
  val previousSchemeOverFalseKIVectorList = Vector(model4, model5, model6)
  val previousSchemeUnderTotalAmount = Vector(model3, model5, model7)

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
  }

  def setupShowMocks(proposedInvestmentModel: Option[ProposedInvestmentModel] = None, previousRFIModel: Option[HadPreviousRFIModel] = None,
                     backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel]
      (Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(proposedInvestmentModel))
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(previousRFIModel))
    when(mockS4lConnector.fetchAndGetFormData[String]
      (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backLink))
  }

  def setupSubmitMocks(ltaExceeded: Option[Boolean] = None, backLink: Option[String] = None,
                       previousRFIModel: Option[HadPreviousRFIModel] = None, kiProcessingModel: Option[KiProcessingModel] = None,
                       previousSchemes: Option[Vector[PreviousSchemeModel]] = None): Unit = {
    when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(ltaExceeded))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkProposedInvestment))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(previousRFIModel))
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(kiProcessingModel))
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(previousSchemes))
  }

  "Sending a GET request to ProposedInvestmentController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(proposedInvestmentModel), Some(hadPreviousRFIModelNo), Some(routes.ReviewPreviousSchemesController.show().url))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore when authenticated and enrolled" in {
      setupShowMocks(backLink = Some(routes.ReviewPreviousSchemesController.show().url))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to ProposedInvestmentController when authenticated and NOT enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(proposedInvestmentModel), Some(hadPreviousRFIModelNo), Some(routes.ReviewPreviousSchemesController.show().url))
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending a GET request to ProposedInvestmentController without a valid backlink from keystore when authenticated and enrolled" should {
    "redirect to the beginning of the flow" in {
      setupShowMocks()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/used-investment-scheme-before")
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to ProposedInvestmentController when authenticated and enrolled" should {
    "return a 302 and redirect to GG login" in {
      mockEnrolledRequest()
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

  "Sending a request with no session to ProposedInvestmentController" should {
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

  "Sending a timed-out request to ProposedInvestmentController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a valid form submit with not exceeding the lifetime allowance (true KI) to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the what will use for page" in {
      setupSubmitMocks(Some(false), Some(routes.ReviewPreviousSchemesController.show().url), Some(hadPreviousRFIModelYes),
        Some(trueKIModel), Some(previousSchemeTrueKIVectorList))
      mockEnrolledRequest()
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit with a not exceeding the lifetime allowance (true KI) and no previous RFI to the ProposedInvestmentController" +
    "when authenticated and enrolled" should {
    "redirect to the what will use for page" in {
      setupSubmitMocks(Some(false), Some(routes.ReviewPreviousSchemesController.show().url), Some(hadPreviousRFIModelNo),
        Some(trueKIModel), Some(previousSchemeTrueKIVectorList))
      mockEnrolledRequest()
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit with exceeded lifetime allowance (true KI) to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the exceeded lifetime limit page" in {
      setupSubmitMocks(Some(true), Some(routes.ReviewPreviousSchemesController.show().url), Some(hadPreviousRFIModelYes),
        Some(trueKIModel), Some(previousSchemeOverTrueKIVectorList))
      mockEnrolledRequest()
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/lifetime-allowance-exceeded")
        }
      )
    }
  }

  "Sending a valid form submit with not exceeding the lifetime allowance (false KI) to the ProposedInvestmentController" +
    " when authenticated and enrolled" should {
    "redirect to the what will do page" in {
      setupSubmitMocks(Some(false), Some(routes.ReviewPreviousSchemesController.show().url), Some(hadPreviousRFIModelYes),
        Some(falseKIModel), Some(previousSchemeFalseKIVectorList))
      mockEnrolledRequest()
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit with exceeded lifetime allowance (false KI) to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the exceeded lifetime limit page" in {
      setupSubmitMocks(Some(true), Some(routes.ReviewPreviousSchemesController.show().url), Some(hadPreviousRFIModelYes),
        Some(falseKIModel), Some(previousSchemeFalseKIVectorList))
      mockEnrolledRequest()
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/lifetime-allowance-exceeded")
        }
      )
    }
  }

  "Sending a valid form submit with not exceeded lifetime allowance (false KI) to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the exceeded lifetime limit page" in {
      setupSubmitMocks(Some(true), Some(routes.ReviewPreviousSchemesController.show().url), Some(hadPreviousRFIModelYes),
        Some(trueKIModel), Some(previousSchemeUnderTotalAmount))
      mockEnrolledRequest()
      val formInput = "investmentAmount" -> "5000000"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/lifetime-allowance-exceeded")
        }
      )
    }
  }

  "Sending a valid form submit with No KIModel to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the DateOfIncorporation page" in {
      setupSubmitMocks(Some(false), Some(routes.ReviewPreviousSchemesController.show().url), Some(hadPreviousRFIModelYes))
      mockEnrolledRequest()
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect with a bad request" in {
      setupSubmitMocks(backLink = Some(routes.ReviewPreviousSchemesController.show().url))
      mockEnrolledRequest()
      val formInput = "investmentAmount" -> "fff"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending an invalid form submission with value 0 to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect with a bad request" in {
      setupSubmitMocks(backLink = Some(routes.ReviewPreviousSchemesController.show().url))
      mockEnrolledRequest()
      val formInput = "investmentAmount" -> "0"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending an invalid form submission with value 5000001 to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect with a bad request" in {
      setupSubmitMocks(backLink = Some(routes.ReviewPreviousSchemesController.show().url))
      mockEnrolledRequest()
      val formInput = "investmentAmount" -> "5000001"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a submission to the NewGeographicalMarketController when not authenticated" should {

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

  "Sending a submission to the NewGeographicalMarketController when a timeout has occurred" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the NewGeographicalMarketController when NOT enrolled" should {
    "redirect to the Subscription Servicec" in {
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
