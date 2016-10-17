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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.FakeRequestHelper
import uk.gov.hmrc.play.test.WithFakeApplication
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite

import scala.concurrent.Future

class IneligibleForKIControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach
  with OneServerPerSuite with WithFakeApplication with FakeRequestHelper{


  val mockS4lConnector = mock[S4LConnector]

  object IneligibleForKIControllerTest extends IneligibleForKIController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(IneligibleForKIControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(IneligibleForKIControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

  override def beforeEach() {
    reset(mockS4lConnector)
  }

  implicit val hc = HeaderCarrier()

  "IneligibleForKIController" should {
    "use the correct keystore connector" in {
      IneligibleForKIController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      IneligibleForKIController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      IneligibleForKIController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to IneligibleForKIController without a valid backlink from keystore when authenticated and enrolled" should {
    "redirect to the beginning of the flow" in {
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkIneligibleForKI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      showWithSessionAndAuth(IneligibleForKIControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/operating-costs")
        }
      )
    }
  }

  "Sending a GET request to IneligibleForKIController with a valid back link when authenticated and enrolled" should {
    "return a 200" in {
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkIneligibleForKI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.OperatingCostsController.show().toString())))
      mockEnrolledRequest
      showWithSessionAndAuth(IneligibleForKIControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to IneligibleForKIController with a valid back link when authenticated and NOT enrolled" should {
    "redirect to the Subscription Service" in {
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkIneligibleForKI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.OperatingCostsController.show().toString())))
      mockNotEnrolledRequest
      showWithSessionAndAuth(IneligibleForKIControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to IneligibleForKIController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(IneligibleForKIControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to IneligibleForKIController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(IneligibleForKIControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to IneligibleForKIController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(IneligibleForKIControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }


  "Posting to the IneligibleForKIController when authenticated and enrolled" should {
    "redirect to 'Subsidiaries' page" in {

      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkIneligibleForKI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.OperatingCostsController.show().toString())))
      mockEnrolledRequest
      submitWithSessionAndAuth(IneligibleForKIControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }

  "Sending a submission to the IneligibleForKIController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(IneligibleForKIControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(IneligibleForKIControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the IneligibleForKIController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(IneligibleForKIControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the IneligibleForKIController when not enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      submitWithSessionAndAuth(IneligibleForKIControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

}
