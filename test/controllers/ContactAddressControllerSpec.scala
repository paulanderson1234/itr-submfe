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
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class ContactAddressControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper {

  val mockS4lConnector = mock[S4LConnector]

  object ContactAddressControllerTest extends ContactAddressController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(ContactAddressControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))

  private def mockNotEnrolledRequest = when(ContactAddressControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

  val model: AddressModel = AddressModel(addressline1 = "line 1",
    addressline2 = "Line 2", addressline3 = Some("Line 3"), addressline4 = Some("Line 4"),
    postcode = Some("TF1 4NY"), countryCode = "GB")

  val emptyModel = AddressModel("", "")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val keyStoreSavedContactAddress = AddressModel(addressline1 = "line 1",
    addressline2 = "Line 2 saved", addressline3 = Some("Line 3 saved"), addressline4 = Some("Line 4 saved"),
    postcode = Some("TF1 4NY"), countryCode = "GB")

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockS4lConnector)
  }

  "ContactAddressController" should {
    "use the correct auth connector" in {
      ContactAddressController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      ContactAddressController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      ContactAddressController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to ContactAddressController when authenticated and enrolled" should {
    "return a 200 OK when something is fetched from keystore" in {
      when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedContactAddress)))
      mockEnrolledRequest
      showWithSessionAndAuth(ContactAddressControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      showWithSessionAndAuth(ContactAddressControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to ContactAddressController when authenticated and NOT enrolled" should {
    "redirect to the Subscription Service" in {
      when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedContactAddress)))
      mockNotEnrolledRequest
      showWithSessionAndAuth(ContactAddressControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to ContactAddressController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(ContactAddressControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to ContactAddressController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(ContactAddressControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to ContactAddressController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(ContactAddressControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }


  "Sending a valid form submit to the ContactAddressController when authenticated and enrolled" should {
    "redirect to the Contact Details Subscription Controller page" in {
      mockEnrolledRequest
      val formInput =
        Seq("addressline1" -> "Line 1",
          "addressline2" -> "Line 2",
          "addressline3" -> "Line 3",
          "addressline4" -> "line 4",
          "postcode" -> "TF1 3NY",
          "countryCode" -> "GB")

      submitWithSessionAndAuth(ContactAddressControllerTest.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SupportingDocumentsController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ContactAddressController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest
      val formInput = Seq("addressLine1" -> "", "addressLine1" -> "", "addressLine3" -> "Line3",
        "addressLine4" -> "Line4", "postCode" -> "LE5 5NN", "countryCode" -> "GB")
      submitWithSessionAndAuth(ContactAddressControllerTest.submit, formInput: _*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a valid form submit to the ContactAddressController when authenticated and enrolled" should {
    "redirect to the Subscription Service" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      mockNotEnrolledRequest
      val formInput = Seq("addressline1" -> "Line1", "addressline2" -> "Line2", "addressline3" -> "Line3",
        "addressline4" -> "Line4", "postCode" -> "LE5 5NN", "countryCode" -> "GB")
      submitWithSessionAndAuth(ContactAddressControllerTest.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }



  "Sending a submission to the ContactAddressController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(ContactAddressControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(ContactAddressControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the ContactAddressController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(ContactAddressControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

}
