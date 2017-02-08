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

package controllers

import java.net.URLEncoder

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.seis.TradeStartDateController
import helpers.ControllerSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class TradeStartDateControllerSpec extends ControllerSpec {

  object TestController extends TradeStartDateController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val submissionConnector = mockSubmissionConnector
  }

  "TradeStartDateController" should {
    "use the correct auth connector" in {
      TradeStartDateController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      TradeStartDateController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      TradeStartDateController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct submission Connector" in {
      TradeStartDateController.submissionConnector shouldBe SubmissionConnector
    }
  }

  def setupShowMocks(tradeStartDateModel: Option[TradeStartDateModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(tradeStartDateModel))

  def setupSubmitMocks(result: Option[Boolean]): Unit = {
    when(mockSubmissionConnector.validateTradeStartDateCondition(Matchers.any(),Matchers.any(),Matchers.any())(Matchers.any())).thenReturn(Future.successful(result))
  }

  "Sending a GET request to TradeStartDateController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(tradeStartDateModelYes))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupShowMocks()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending an Unauthenticated request with a session to TradeStartDateController" should {
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

  "Sending a request with no session to TradeStartDateController" should {
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

  "Sending a timed-out request to TradeStartDateController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a NOT enrolled request" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending a valid Yes form submission to the TradeStartDateController when authenticated and enrolled" should {
    "redirect to the Used Investment Reason Before page if the Trade start date condition is met" in {
      val formInput = Seq(
        "hasTradeStartDate" -> Constants.StandardRadioButtonYesValue,
        "tradeStartDay" -> "23",
        "tradeStartMonth" -> "11",
        "tradeStartYear" -> "2015")
      setupSubmitMocks(Some(true))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit,formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/seis/used-investment-scheme-before")
        }
      )
    }
  }

  "Sending a valid Yes form submission to the TradeStartDateController when authenticated and enrolled" should {
    "redirect to the Used Investment Reason Before page if the Trade Start date condition is not met" in {
      val formInput = Seq(
        "hasTradeStartDate" -> Constants.StandardRadioButtonYesValue,
        "tradeStartDay" -> "23",
        "tradeStartMonth" -> "11",
        "tradeStartYear" -> "2014")
      setupSubmitMocks(Some(false))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/seis/trade-start-date-error")
        }
      )
    }
  }

  "Sending a valid No form submission to the TradeStartDateController when authenticated and enrolled" should {
    "redirect to the Used Investment Reason Before page" in {
      val formInput = Seq(
        "hasTradeStartDate" -> Constants.StandardRadioButtonNoValue)
      setupSubmitMocks(Some(true))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/seis/used-investment-scheme-before")
        }
      )
    }
  }
  "Sending a valid Yes form submission to the TradeStartDateController when authenticated and enrolled" should {
      "redirect to the INTERNAL_SERVER_ERROR page as the API call has failed" in {
        val formInput = Seq(
          "hasTradeStartDate" -> Constants.StandardRadioButtonYesValue,
          "tradeStartDay" -> "23",
          "tradeStartMonth" -> "11",
          "tradeStartYear" -> "2014")
      setupSubmitMocks(None)
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }


  "Sending a submission to the TradeStartDateController when not authenticated" should {

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

  "Sending a submission to the TradeStartDateController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the TradeStartDateController when not enrolled" should {

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
