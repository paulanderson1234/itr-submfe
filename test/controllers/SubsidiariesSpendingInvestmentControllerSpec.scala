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

import auth.{Enrolment, Identifier, MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import helpers.FakeRequestHelper
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class SubsidiariesSpendingInvestmentControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper {

  val mockS4lConnector = mock[S4LConnector]

  object SubsidiariesSpendingInvestmentControllerTest extends SubsidiariesSpendingInvestmentController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  val modelYes = SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonYesValue)
  val modelNo = SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonNoValue)
  val emptyModel = SubsidiariesSpendingInvestmentModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(modelYes)))
  val keyStoreSavedSubsidiariesSpendingInvestment = SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonYesValue)

  private def mockEnrolledRequest = when(SubsidiariesSpendingInvestmentControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(SubsidiariesSpendingInvestmentControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockS4lConnector)
  }

  "SubsidiariesSpendingInvestmentController" should {
    "use the correct keystore connector" in {
      SubsidiariesSpendingInvestmentController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      SubsidiariesSpendingInvestmentController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      SubsidiariesSpendingInvestmentController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to SubsidiariesSpendingInvestmentController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesSpendingInvestment)))
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(routes.WhatWillUseForController.show().url)))
      mockEnrolledRequest
      showWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore when authenticated and enrolled" in {
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(routes.WhatWillUseForController.show().url)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      mockEnrolledRequest
      showWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 300 when no back link is fetched using keystore when authenticated and enrolled" in {
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      showWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a GET request to SubsidiariesSpendingInvestmentController when authenticated and NOT enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesSpendingInvestment)))
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(routes.WhatWillUseForController.show().url)))
      mockNotEnrolledRequest
      showWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to SubsidiariesSpendingInvestmentController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(SubsidiariesSpendingInvestmentControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to SubsidiariesSpendingInvestmentController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(SubsidiariesSpendingInvestmentControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to SubsidiariesSpendingInvestmentController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(SubsidiariesSpendingInvestmentControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the SubsidiariesSpendingInvestmentController when authenticated and enrolled" should {
    "redirect to the subsidiaries-ninety-percent-owned page" in {
      mockEnrolledRequest
      val formInput = "subSpendingInvestment" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries-ninety-percent-owned")
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the SubsidiariesSpendingInvestmentController when authenticated and enrolled" should {
    "redirect to the how-plan-to-use-investment page" in {
      mockEnrolledRequest
      val formInput = "subSpendingInvestment" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/how-plan-to-use-investment")
        }
      )
    }
  }

  "Sending a invalid form submit to the SubsidiariesSpendingInvestmentController with no back link when authenticated and enrolled" should {
    "redirect to the subsidiaries-ninety-percent-owned page" in {
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      val formInput = "subSpendingInvestment" -> ""
      submitWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the SubsidiariesSpendingInvestmentController when authenticated and enrolled" should {
    "redirect to itself with errors" in {
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(routes.WhatWillUseForController.show().url)))
      mockEnrolledRequest
      val formInput = "subSpendingInvestment" -> ""
      submitWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a submission to the SubsidiariesSpendingInvestmentController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(SubsidiariesSpendingInvestmentControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(SubsidiariesSpendingInvestmentControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the SubsidiariesSpendingInvestmentController when a timeout has occurred" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(SubsidiariesSpendingInvestmentControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the SubsidiariesSpendingInvestmentController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      submitWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }
}
