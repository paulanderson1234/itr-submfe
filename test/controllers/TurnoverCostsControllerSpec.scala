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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import helpers.ControllerSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class TurnoverCostsControllerSpec extends ControllerSpec {

  object TestController extends TurnoverCostsController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupShowMocks(turnoverCostsModel: Option[AnnualTurnoverCostsModel] = None, checkAveragedAnnualTurnover: Option[Boolean] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(turnoverCostsModel))

    // Change to checkAveragedAnnualTurnover method below when ready and perform additional tests
    when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(checkAveragedAnnualTurnover))
  }

  def setupSubmitMocks(proposedInvestmentModel: Option[ProposedInvestmentModel] = None,
                       subsidiariesModel: Option[SubsidiariesModel] = None, checkedTurnover: Option[Boolean] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(proposedInvestmentModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesModel))
    when(mockSubmissionConnector.checkAveragedAnnualTurnover(Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(checkedTurnover))
  }

  "TurnoverCostsController" should {
    "use the correct keystore connector" in {
      TurnoverCostsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      TurnoverCostsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      TurnoverCostsController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct submission connector" in {
      TurnoverCostsController.submissionConnector shouldBe SubmissionConnector
    }
  }

  "Sending a GET formInput to TurnoverCostsController when Authenticated and enrolled" when {

    "The AnnualTurnoverCostsModel can be obtained from keystore" should {
      "return an OK" in {
        setupShowMocks(Some(annualTurnoverCostsModel), Some(true))
        mockEnrolledRequest()
        showWithSessionAndAuth(TestController.show())(
          result => status(result) shouldBe OK
        )
      }
    }

    "The AnnualTurnoverCostsModel can't be obtained from keystore" should {
      "return an OK" in {
        setupShowMocks(Some(annualTurnoverCostsModel), Some(true))
        mockEnrolledRequest()
        showWithSessionAndAuth(TestController.show())(
          result => status(result) shouldBe OK
        )
      }
    }

    "Sending a GET formInput to TurnoverCostsController when Authenticated and NOT enrolled" should {
      "redirect to the Subscription Service" in {
        setupShowMocks(Some(annualTurnoverCostsModel))
        mockNotEnrolledRequest()
        showWithSessionAndAuth(TestController.show())(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
          }
        )
      }
    }

    "Sending an Unauthenticated formInput with a session to TurnoverCostsController when Authenticated and enrolled" should {
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

    "Sending a formInput with no session to TurnoverCostsController when Authenticated and enrolled" should {
      "return a 302 and redirect to GG login" in {
        mockEnrolledRequest()
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

    "Sending a timed-out formInput to TurnoverCostsController when Authenticated and enrolled" should {
      "return a 302 and redirect to the timeout page" in {
        mockEnrolledRequest()
        showWithTimeout(TestController.show())(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
          }
        )
      }
    }

    "Sending a valid form submission to the TurnoverCostsController when Authenticated and enrolled" should {
      "redirect to subsidiariess spending investment form when annual turnover check returns true and owns subsidiaries is true" in {
        mockEnrolledRequest()
        setupSubmitMocks(Some(proposedInvestmentModel), Some(subsidiariesModelYes), Some(true))
        val formInput = Seq(
          "amount1" -> "100",
          "amount2" -> "100",
          "amount3" -> "100",
          "amount4" -> "100",
          "amount5" -> "100",
          "firstYear" -> "2003",
          "secondYear" -> "2004",
          "thirdYear" -> "2005",
          "fourthYear" -> "2006",
          "fifthYear" -> "2007"
        )
        submitWithSessionAndAuth(TestController.submit, formInput: _*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries-spending-investment")
          }
        )
      }

        "redirect to investment grow form when annual turnover check returns true and owns subsidiaries is false" in {
          mockEnrolledRequest()
          setupSubmitMocks(Some(proposedInvestmentModel), Some(subsidiariesModelNo), Some(true))
          val formInput = Seq(
            "amount1" -> "100",
            "amount2" -> "100",
            "amount3" -> "100",
            "amount4" -> "100",
            "amount5" -> "100",
            "firstYear" -> "2003",
            "secondYear" -> "2004",
            "thirdYear" -> "2005",
            "fourthYear" -> "2006",
            "fifthYear" -> "2007"
          )
          submitWithSessionAndAuth(TestController.submit, formInput: _*)(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some("/investment-tax-relief/how-plan-to-use-investment")
            }
          )
        }

      "redirect to annual turnover error page when annual turnover check returns false" in {
        mockEnrolledRequest()
        setupSubmitMocks(Some(proposedInvestmentModel), Some(subsidiariesModelNo), Some(false))
        val formInput = Seq(
          "amount1" -> "100",
          "amount2" -> "100",
          "amount3" -> "100",
          "amount4" -> "100",
          "amount5" -> "100",
          "firstYear" -> "2003",
          "secondYear" -> "2004",
          "thirdYear" -> "2005",
          "fourthYear" -> "2006",
          "fifthYear" -> "2007"
        )
        submitWithSessionAndAuth(TestController.submit, formInput: _*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/annual-turnover-error")
          }
        )
      }

      "redirect to proposed investment page when no proposed investment is returned from keystore" in {
        mockEnrolledRequest()
        setupSubmitMocks(subsidiariesModel = Some(subsidiariesModelYes))
        val formInput = Seq(
          "amount1" -> "100",
          "amount2" -> "100",
          "amount3" -> "100",
          "amount4" -> "100",
          "amount5" -> "100",
          "firstYear" -> "2003",
          "secondYear" -> "2004",
          "thirdYear" -> "2005",
          "fourthYear" -> "2006",
          "fifthYear" -> "2007"
        )
        submitWithSessionAndAuth(TestController.submit, formInput: _*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/proposed-investment")
          }
        )
      }

      "redirect to subsidiaries page when no subsidiaries model is returned from keystore" in {
        mockEnrolledRequest()
        setupSubmitMocks(Some(proposedInvestmentModel),checkedTurnover = Some(true))
        val formInput = Seq(
          "amount1" -> "100",
          "amount2" -> "100",
          "amount3" -> "100",
          "amount4" -> "100",
          "amount5" -> "100",
          "firstYear" -> "2003",
          "secondYear" -> "2004",
          "thirdYear" -> "2005",
          "fourthYear" -> "2006",
          "fifthYear" -> "2007"
        )
        submitWithSessionAndAuth(TestController.submit, formInput: _*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
          }
        )
      }


    }

    "Sending an invalid form submit to the TurnoverCostsController when Authenticated and enrolled" should {
      "return a bad request" in {
        setupSubmitMocks(subsidiariesModel = Some(subsidiariesModelNo))
        mockEnrolledRequest()
        val formInput = Seq(
          "amount1" -> "",
          "amount2" -> "",
          "amount3" -> "",
          "amount4" -> "",
          "amount5" -> "",
          "firstYear" -> "2003",
          "secondYear" -> "2004",
          "thirdYear" -> "2005",
          "fourthYear" -> "2006",
          "fifthYear" -> "2007"
        )
        submitWithSessionAndAuth(TestController.submit, formInput: _*)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }

    "Sending a submission to the TurnoverCostsController when not authenticated" should {

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
  }

  "Sending a submission to the TurnoverCostsController when a timeout has occurred" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the TurnoverCostsController when NOT enrolled" should {
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