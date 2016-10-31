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
import models.ConfirmContactDetailsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class ConfirmContactDetailsControllerSpec extends ControllerSpec {

  object TestController extends ConfirmContactDetailsController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks
  (
    confirmContactDetailsModel: Option[ConfirmContactDetailsModel] = None
  ): Unit = {
    when(TestController.s4lConnector.fetchAndGetFormData[ConfirmContactDetailsModel](Matchers.eq(KeystoreKeys.confirmContactDetails))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(confirmContactDetailsModel))
  }

  "ConfirmContactDetailsController" should {
    "use the correct auth connector" in {
      ConfirmContactDetailsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      ConfirmContactDetailsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      ConfirmContactDetailsController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to ConfirmContactDetailsController" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(confirmContactDetailsModel))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to ConfirmContactDetailsController" +
    "where there is no data already stored" should {
    "return a 200 when nothing is retrieved from Keystore" in {
      setupMocks()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  //TODO: Re-introduce when we try to get the contactDetails from ETMP when it is then possible to mock a return none for this
//    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
//      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
//      when(mockKeyStoreConnector.fetchAndGetFormData[ConfirmContactDetailsModel](Matchers.any())(Matchers.any(), Matchers.any()))
//        .thenReturn(Future.successful(None))
//      mockEnrolledRequest
//      showWithSessionAndAuth(ConfirmContactDetailsControllerTest.show())(
//        result => status(result) shouldBe OK
//      )
//    }
//  }

  "Sending an Authenticated and NOT Enrolled GET request with a session to ConfirmContactDetailsController" should {

    "return a 303 to the subscription url" in {
      setupMocks(Some(confirmContactDetailsModel))
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to ConfirmContactDetailsController" should {
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

  "Sending a request with no session to ConfirmContactDetailsController" should {
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

  "Sending a timed-out request to ConfirmContactDetailsController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }


  "Submitting a valid form submission to ConfirmContactDetailsController while authenticated and enrolled" should {
    "redirect Confirm Correspondence Address Page when the Yes option is selected" in {
      setupMocks(Some(confirmContactDetailsModel))
      mockEnrolledRequest()
      val formInput = Seq(
        "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
        "contactDetails.forename" -> "Hank",
        "contactDetails.surname" -> "The Tank",
        "contactDetails.telephoneNumber" -> "01234 567890",
        "contactDetails.mobileNumber" -> "01234 567890",
        "contactDetails.email" -> "thisiavalidemail@valid.com")
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ConfirmCorrespondAddressController.show().url)
        }
      )
    }
  }
    "Submitting a valid form submission to ConfirmContactDetailsController while authenticated and enrolled" should {
    "redirect to Contact Address page when the 'No' option is selected" in {
      setupMocks(Some(confirmContactDetailsModel))
      mockEnrolledRequest()
      val formInput = Seq(
        "contactDetailsUse" -> Constants.StandardRadioButtonNoValue,
        "contactDetails.forename" -> "Hank",
        "contactDetails.surname" -> "The Tank",
        "contactDetails.telephoneNumber" -> "01234 567890",
        "contactDetails.mobileNumber" -> "01234 567890",
        "contactDetails.email" -> "thisiavalidemail@valid.com")
      submitWithSessionAndAuth(TestController.submit, formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ContactDetailsController.show().url)
        }
      )
    }
  }

  "Submitting a invalid form submission to ConfirmContactDetailsController while authenticated and enrolled" should {
    "redirect to itself when there is validation errors" in {
      setupMocks(Some(confirmContactDetailsModel))
      mockEnrolledRequest()
      val formInput = Seq(
        "contactDetailsUse" -> "",
        "contactDetails.forename" -> "Hank",
        "contactDetails.surname" -> "The Tank",
        "contactDetails.telephoneNumber" -> "01234 567890",
        "contactDetails.mobileNumber" -> "01234 567890",
        "contactDetails.email" -> "thisiavalidemail@valid.com")
       submitWithSessionAndAuth(TestController.submit, formInput:_*)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }

  "Submitting a form to ConfirmContactDetailsController with a session but not authenticated" should {

    val formInput = "contactAddressUse" -> Constants.StandardRadioButtonYesValue
    "return a 303 and redirect to the GG login page" in {
      setupMocks(Some(confirmContactDetailsModel))
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

  "Submitting a form to ConfirmContactDetailsController with no session" should {

    val formInput = "contactAddressUse" -> Constants.StandardRadioButtonYesValue
    "return a 303 and redirect to the GG login page" in {
      setupMocks(Some(confirmContactDetailsModel))
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

  "Submitting a form to ConfirmContactDetailsController with a timeout" should {

    val formInput = "contactAddressUse" -> Constants.StandardRadioButtonYesValue
    "return a 303 and redirect to the timeout page" in {
      setupMocks(Some(confirmContactDetailsModel))
      submitWithTimeout(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Submitting a form to ConfirmContactDetailsController when NOT enrolled" should {
    "return a 303 and redirect to the Subscription Service" in {
      setupMocks(Some(confirmContactDetailsModel))
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
