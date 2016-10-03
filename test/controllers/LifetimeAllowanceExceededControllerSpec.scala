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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, KeystoreConnector}
import controllers.helpers.FakeRequestHelper
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future


class LifetimeAllowanceExceededControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication with FakeRequestHelper{
  

  implicit val hc = HeaderCarrier()

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object LifetimeAllowanceExceededControllerTest extends LifetimeAllowanceExceededController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(LifetimeAllowanceExceededControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(LifetimeAllowanceExceededControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

  "LifetimeAllowanceExceededController" should {
    "use the correct keystore connector" in {
      LifetimeAllowanceExceededController.keyStoreConnector shouldBe KeystoreConnector
    }
    "use the correct auth connector" in {
      LifetimeAllowanceExceededController.authConnector shouldBe FrontendAuthConnector
    }
  }

  "Sending a GET request to LifetimeAllowanceExceededController when authenticated and enrolled" should {
    "return a 200" in {
      mockEnrolledRequest
      showWithSessionAndAuth(LifetimeAllowanceExceededControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to LifetimeAllowanceExceededController when authenticated and NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      showWithSessionAndAuth(LifetimeAllowanceExceededControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to LifetimeAllowanceExceededController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(LifetimeAllowanceExceededControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to LifetimeAllowanceExceededController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(LifetimeAllowanceExceededControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to LifetimeAllowanceExceededController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(LifetimeAllowanceExceededControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Posting to the LifetimeAllowanceExceededController when authenticated and enrolled" should {
    "redirect to 'Proposed investment' page" in {
      mockEnrolledRequest
      submitWithSessionAndAuth(LifetimeAllowanceExceededControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/proposed-investment")
        }
      )
    }
  }

  "Sending a submission to the LifetimeAllowanceExceededController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(LifetimeAllowanceExceededControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(LifetimeAllowanceExceededControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the LifetimeAllowanceExceededController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(LifetimeAllowanceExceededControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the LifetimeAllowanceExceededController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      submitWithSessionAndAuth(LifetimeAllowanceExceededControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

}
