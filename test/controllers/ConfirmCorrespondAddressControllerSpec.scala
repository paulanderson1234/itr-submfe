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
import controllers.helpers.ControllerSpec
import models.{AddressModel, ConfirmCorrespondAddressModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class ConfirmCorrespondAddressControllerSpec extends ControllerSpec {

  object TestController extends ConfirmCorrespondAddressController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(confirmCorrespondAddressModel: Option[ConfirmCorrespondAddressModel] = None, addressModel: Option[AddressModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.eq(KeystoreKeys.confirmContactAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(confirmCorrespondAddressModel))
    when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(addressModel))
  }

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

  "Sending an Authenticated and Enrolled GET request with a session to ConfirmCorrespondAddressController" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(confirmCorrespondAddressModel),Some(addressModel))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  //TODO: mock the addressModel returned form ETMP when implemented (currently is hard coded return in controller)
  "Sending an Authenticated and Enrolled GET request with a session to ConfirmCorrespondAddressController" should {
    "return a 200 when no contact address exists but an address is returned from ETMP" in {
      setupMocks(Some(confirmCorrespondAddressModel))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }


  //TODO: Re-introduce when we try to get the addressModel form ETMP when it is then possible to mock a return none for this
//    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
//      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
//      when(mockKeyStoreConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
//        .thenReturn(Future.successful(None))
//      mockEnrolledRequest
//      showWithSessionAndAuth(ConfirmCorrespondAddressControllerTest.show())(
//        result => status(result) shouldBe OK
//      )
//    }
//  }

  "Sending an Authenticated and NOT Enrolled GET request with a session to ConfirmCorrespondAddressController" should {

    "return a 303 to the subscription url" in {
      setupMocks(Some(confirmCorrespondAddressModel),Some(addressModel))
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
      mockEnrolledRequest()
      val formInput = Seq(
        "contactAddressUse" -> Constants.StandardRadioButtonYesValue,
        "address.addressline1" -> "Line 1",
        "address.addressline2" -> "Line 2",
        "address.addressline3" -> "Line 3",
        "address.addressline4" -> "line 4",
        "address.postcode" -> "TF1 3NY",
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
      mockEnrolledRequest()
      val formInput = Seq(
        "contactAddressUse" -> Constants.StandardRadioButtonNoValue,
        "address.addressline1" -> "Line 1",
        "address.addressline2" -> "Line 2",
        "address.addressline3" -> "Line 3",
        "address.addressline4" -> "line 4",
        "address.postcode" -> "TF1 3NY",
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
      mockEnrolledRequest()
      val formInput = Seq(
        "contactAddressUse" -> "",
        "address.addressline1" -> "Line 1",
        "address.addressline2" -> "Line 2",
        "address.addressline3" -> "Line 3",
        "address.addressline4" -> "line 4",
        "address.postcode" -> "TF1 3NY",
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
