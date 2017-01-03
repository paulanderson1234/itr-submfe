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
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.ControllerSpec
import models.{AddressModel, ConfirmCorrespondAddressModel, SubscriptionDetailsModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.SubscriptionService
import data.SubscriptionTestData._

import scala.concurrent.Future

class ConfirmCorrespondAddressControllerSpec extends ControllerSpec {

  object TestController extends ConfirmCorrespondAddressController {
    override lazy val subscriptionService = mock[SubscriptionService]
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupSaveForLaterMocks
  (
    confirmCorrespondAddressModel: Option[ConfirmCorrespondAddressModel] = None,
    backLink: Option[String] = None
  ): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.eq(KeystoreKeys.confirmContactAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(confirmCorrespondAddressModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkConfirmCorrespondence))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(backLink))
  }

  def mockSubscriptionServiceResponse(address: Option[AddressModel] = None): Unit =
    when(TestController.subscriptionService.getSubscriptionContactAddress(Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(address))

  "ConfirmCorrespondAddressController" should {
    "use the correct auth connector" in {
      ConfirmCorrespondAddressController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      ConfirmCorrespondAddressController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      ConfirmCorrespondAddressController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to ConfirmCorrespondAddressController" when {

    "there is address data stored in S4L" should {

      lazy val result = await(TestController.show()(authorisedFakeRequest))
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a Status OK (200) when something is fetched from keystore" in {
        setupSaveForLaterMocks(Some(confirmCorrespondAddressModel), Some("back-link"))
        mockSubscriptionServiceResponse()
        mockEnrolledRequest()
        status(result) shouldBe OK
      }

      "Use the stored S4L addressline1 on the rendered page" in {
        document.getElementById("address.addressline1").attr("value") shouldBe addressModel.addressline1
      }

      "Use the stored S4L addressline2 on the rendered page" in {
        document.getElementById("address.addressline2").attr("value") shouldBe addressModel.addressline2
      }

      "Use the stored S4L addressline3 on the rendered page" in {
        document.getElementById("address.addressline3").attr("value") shouldBe addressModel.addressline3.get
      }

      "Use the stored S4L addressline4 on the rendered page" in {
        document.getElementById("address.addressline4").attr("value") shouldBe addressModel.addressline4.get
      }

      "Use the stored S4L postcode on the rendered page" in {
        document.getElementById("address.postcode").attr("value") shouldBe addressModel.postcode.get
      }

      "Use the stored S4L countryCode on the rendered page" in {
        document.getElementById("address.countryCode").attr("value") shouldBe addressModel.countryCode
      }
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to ConfirmContactDetailsController" when {

    "there is no data stored in S4L and data from Subscription Service is retrieved" should {

      lazy val result = await(TestController.show()(authorisedFakeRequest))
      lazy val document = Jsoup.parse(bodyOf(result))

      "return Status OK (200)" in {
        setupSaveForLaterMocks(None, Some("back-link"))
        mockEnrolledRequest()
        mockSubscriptionServiceResponse(Some(expectedContactAddressFull))
        status(result) shouldBe OK
      }

      "Use the ETMP addressline1 on the rendered page" in {
        document.getElementById("address.addressline1").attr("value") shouldBe expectedContactAddressFull.addressline1
      }

      "Use the ETMP addressline2 on the rendered page" in {
        document.getElementById("address.addressline2").attr("value") shouldBe expectedContactAddressFull.addressline2
      }

      "Use the ETMP addressline3 on the rendered page" in {
        document.getElementById("address.addressline3").attr("value") shouldBe expectedContactAddressFull.addressline3.get
      }

      "Use the ETMP addressline4 on the rendered page" in {
        document.getElementById("address.addressline4").attr("value") shouldBe expectedContactAddressFull.addressline4.get
      }

      "Use the ETMP postcode on the rendered page" in {
        document.getElementById("address.postcode").attr("value") shouldBe expectedContactAddressFull.postcode.get
      }

      "Use the ETMP countryCode on the rendered page" in {
        document.getElementById("address.countryCode").attr("value") shouldBe expectedContactAddressFull.countryCode
      }
    }

    "there is no data stored in S4L and no data returned from ETMP" should {

      lazy val result = await(TestController.show()(authorisedFakeRequest))
      lazy val document = Jsoup.parse(bodyOf(result))

      "return INTERNAL_SERVER_ERROR (500)" in {
        setupSaveForLaterMocks(None, Some("back-link"))
        mockEnrolledRequest()
        mockSubscriptionServiceResponse()
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "Sending an Authenticated and NOT Enrolled GET request with a session to ConfirmCorrespondAddressController" should {

    "return a 303 to the subscription url" in {
      setupSaveForLaterMocks(Some(confirmCorrespondAddressModel),Some("backLink"))
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to ConfirmCorrespondAddressController" should {
    "return a 302 and redirect to the GG login page" in {
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

  "Sending a request with no session to ConfirmCorrespondAddressController" should {
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

  "Sending a timed-out request to ConfirmCorrespondAddressController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }


  "Submitting a valid form submission to ConfirmCorrespondAddressController while authenticated and enrolled" should {
    "redirect Supporting Documents when the Yes option is selected" in {
      setupSaveForLaterMocks(Some(confirmCorrespondAddressModel),Some("backLink"))
      mockEnrolledRequest()
      val formInput = Seq(
        "contactAddressUse" -> Constants.StandardRadioButtonYesValue,
        "address.addressline1" -> "Line 1",
        "address.addressline2" -> "Line 2",
        "address.addressline3" -> "Line 3",
        "address.addressline4" -> "line 4",
        "address.postcode" -> "AA1 1AA",
        "address.countryCode" -> "GB")

      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/supporting-documents")
        }
      )
    }
  }
    "Submitting a valid form submission to ConfirmCorrespondAddressController while authenticated and enrolled" should {
    "redirect to Contact Address page when the 'No' option is selected" in {
      setupSaveForLaterMocks(Some(confirmCorrespondAddressModel),Some("backLink"))
      mockEnrolledRequest()
      val formInput = Seq(
        "contactAddressUse" -> Constants.StandardRadioButtonNoValue,
        "address.addressline1" -> "Line 1",
        "address.addressline2" -> "Line 2",
        "address.addressline3" -> "Line 3",
        "address.addressline4" -> "line 4",
        "address.postcode" -> "AA1 1AA",
        "address.countryCode" -> "GB")

      submitWithSessionAndAuth(TestController.submit, formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/contact-address")
        }
      )
    }
  }

  "Submitting a invalid form submission to ConfirmCorrespondAddressController while authenticated and enrolled" should {
    "redirect to itself when there is validation errors" in {
      setupSaveForLaterMocks(Some(confirmCorrespondAddressModel),Some("backLink"))
      mockEnrolledRequest()
      val formInput = Seq(
        "contactAddressUse" -> "",
        "address.addressline1" -> "Line 1",
        "address.addressline2" -> "Line 2",
        "address.addressline3" -> "Line 3",
        "address.addressline4" -> "line 4",
        "address.postcode" -> "AA1 1AA",
        "address.countryCode" -> "GB")
       submitWithSessionAndAuth(TestController.submit, formInput:_*)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }

  "Submitting a form to ConfirmCorrespondAddressController with a session but not authenticated" should {

    val formInput = "contactAddressUse" -> Constants.StandardRadioButtonYesValue
    "return a 303 and redirect to the GG login page" in {
      setupSaveForLaterMocks(Some(confirmCorrespondAddressModel),Some("backLink"))
      submitWithSessionWithoutAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Submitting a form to ConfirmCorrespondAddressController with no session" should {

    val formInput = "contactAddressUse" -> Constants.StandardRadioButtonYesValue
    "return a 303 and redirect to the GG login page" in {
      setupSaveForLaterMocks(Some(confirmCorrespondAddressModel),Some("backLink"))
      submitWithoutSession(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Submitting a form to ConfirmCorrespondAddressController with a timeout" should {

    val formInput = "contactAddressUse" -> Constants.StandardRadioButtonYesValue
    "return a 303 and redirect to the timeout page" in {
      setupSaveForLaterMocks(Some(confirmCorrespondAddressModel),Some("backLink"))
      submitWithTimeout(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Submitting a form to ConfirmCorrespondAddressController when NOT enrolled" should {
    "return a 303 and redirect to the Subscription Service" in {
      setupSaveForLaterMocks(Some(confirmCorrespondAddressModel),Some("backLink"))
      mockNotEnrolledRequest()
      val formInput = "contactAddressUse" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }
}
