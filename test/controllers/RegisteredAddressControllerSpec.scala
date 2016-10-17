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
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.FakeRequestHelper
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

class RegisteredAddressControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper{

  val mockS4lConnector = mock[S4LConnector]

  object RegisteredAddressControllerTest extends RegisteredAddressController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(RegisteredAddressControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(RegisteredAddressControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

  val model = RegisteredAddressModel("TF1 3NY")
  val emptyModel = RegisteredAddressModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val keyStoreSavedRegisteredAddress = RegisteredAddressModel("LE5 5NN")

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockS4lConnector)
  }

  "RegisteredAddressController" should {
    "use the correct keystore connector" in {
      RegisteredAddressController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      RegisteredAddressController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      RegisteredAddressController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to RegisteredAddressController when authenticated and enrolled" should {
    "return a 200 OK when something is fetched from keystore" in {
      when(mockS4lConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedRegisteredAddress)))
      mockEnrolledRequest
      showWithSessionAndAuth(RegisteredAddressControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore when authenticated and enrolled" in {
      when(mockS4lConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      showWithSessionAndAuth(RegisteredAddressControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to RegisteredAddressController when authenticated and NOT enrolled" should {
    "redirect to the Subscription Service" in {
      when(mockS4lConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedRegisteredAddress)))
      mockNotEnrolledRequest
      showWithSessionAndAuth(RegisteredAddressControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to RegisteredAddressController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(RegisteredAddressControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to RegisteredAddressController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(RegisteredAddressControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to RegisteredAddressController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(RegisteredAddressControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a valid form submit to the RegisteredAddressController when authenticated and enrolled" should {
    "redirect to the commercial sale page" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      mockEnrolledRequest
      val formInput = "postcode" -> "LE5 5NN"
      submitWithSessionAndAuth(RegisteredAddressControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the RegisteredAddressController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest
      val formInput = "postcode" -> ""
      submitWithSessionAndAuth(RegisteredAddressControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a submission to the RegisteredAddressController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(RegisteredAddressControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(RegisteredAddressControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the RegisteredAddressController when a timeout has occurred" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(RegisteredAddressControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the RegisteredAddressController when NOT enrolled" should {
    "redirect to the Timeout page when session has timed out" in {
      mockNotEnrolledRequest
      submitWithSessionAndAuth(RegisteredAddressControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }
}
