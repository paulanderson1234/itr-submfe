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
import controllers.helpers.FakeRequestHelper
import models.{AddressModel, ConfirmCorrespondAddressModel}
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

class ConfirmCorrespondAddressControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper {

  val mockS4lConnector = mock[S4LConnector]


  object ConfirmCorrespondAddressControllerTest extends ConfirmCorrespondAddressController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(ConfirmCorrespondAddressControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(ConfirmCorrespondAddressControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

  val address = AddressModel("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), Some("TF1 5NY"), "GB")
  val model = ConfirmCorrespondAddressModel(Constants.StandardRadioButtonYesValue, address)
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val cacheMapAddress: CacheMap = CacheMap("", Map("" -> Json.toJson(address)))
  val keyStoreSavedConfirmCorrespondAddress = ConfirmCorrespondAddressModel(Constants.StandardRadioButtonYesValue, address)
  val keyStoreSavedContactAddress = AddressModel("c Line 1", "c Line 2", Some("c Line 3"), Some("c Line 4"), Some("TF1 5NY"), "GB")

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockS4lConnector)
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
      when(mockS4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.eq(KeystoreKeys.confirmContactAddress))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedConfirmCorrespondAddress)))

      when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(address)))

      mockEnrolledRequest
      showWithSessionAndAuth(ConfirmCorrespondAddressControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  //TODO: mock the address returned form ETMP when implemented (currently is hard coded return in controller)
  "Sending an Authenticated and Enrolled GET request with a session to ConfirmCorrespondAddressController" should {
    "return a 200 when no contact address exists but an address is returned from ETMP" in {

      when(mockS4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.eq(KeystoreKeys.confirmContactAddress))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedConfirmCorrespondAddress)))

      when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

      mockEnrolledRequest
      showWithSessionAndAuth(ConfirmCorrespondAddressControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }
  }


  //TODO: Re-introduce when we try to get the address form ETMP when it is then possible to mock a return none for this
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
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.eq(KeystoreKeys.confirmContactAddress))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedConfirmCorrespondAddress)))
      when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(address)))
      mockNotEnrolledRequest
      showWithSessionAndAuth(ConfirmCorrespondAddressControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to ConfirmCorrespondAddressController" should {
    "return a 302 and redirect to the GG login page" in {
      showWithSessionWithoutAuth(ConfirmCorrespondAddressControllerTest.show())(
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
      showWithoutSession(ConfirmCorrespondAddressControllerTest.show())(
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
      showWithTimeout(ConfirmCorrespondAddressControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }


  "Submitting a valid form submission to ConfirmCorrespondAddressController while authenticated and enrolled" should {
    "redirect Supporting Documents when the Yes option is selected" in {
      mockEnrolledRequest
      when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.confirmContactAddress), Matchers.any())
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.contactAddress), Matchers.any())
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMapAddress)
      val formInput = Seq(
        "contactAddressUse" -> Constants.StandardRadioButtonYesValue,
        "address.addressline1" -> "Line 1",
        "address.addressline2" -> "Line 2",
        "address.addressline3" -> "Line 3",
        "address.addressline4" -> "line 4",
        "address.postcode" -> "TF1 3NY",
        "address.countryCode" -> "GB")

      submitWithSessionAndAuth(ConfirmCorrespondAddressControllerTest.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/supporting-documents")
        }
      )
    }
  }
    "Submitting a valid form submission to ConfirmCorrespondAddressController while authenticated and enrolled" should {
    "redirect to Contact Address page when the 'No' option is selected" in {
      mockEnrolledRequest
      when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.confirmContactAddress), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.contactAddress), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMapAddress)

      val formInput = Seq(
        "contactAddressUse" -> Constants.StandardRadioButtonNoValue,
        "address.addressline1" -> "Line 1",
        "address.addressline2" -> "Line 2",
        "address.addressline3" -> "Line 3",
        "address.addressline4" -> "line 4",
        "address.postcode" -> "TF1 3NY",
        "address.countryCode" -> "GB")

      submitWithSessionAndAuth(ConfirmCorrespondAddressControllerTest.submit, formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/contact-address")
        }
      )
    }
  }

  "Submitting a invalid form submission to ConfirmCorrespondAddressController while authenticated and enrolled" should {
    "redirect to itself when there is validation errors" in {
      mockEnrolledRequest
      val formInput = Seq(
        "contactAddressUse" -> "",
        "address.addressline1" -> "Line 1",
        "address.addressline2" -> "Line 2",
        "address.addressline3" -> "Line 3",
        "address.addressline4" -> "line 4",
        "address.postcode" -> "TF1 3NY",
        "address.countryCode" -> "GB")
       submitWithSessionAndAuth(ConfirmCorrespondAddressControllerTest.submit, formInput:_*)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }

  "Submitting a form to ConfirmCorrespondAddressController with a session but not authenticated" should {

    val formInput = "contactAddressUse" -> Constants.StandardRadioButtonYesValue
    "return a 303 and redirect to the GG login page" in {
      submitWithSessionWithoutAuth(ConfirmCorrespondAddressControllerTest.submit, formInput)(
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
      submitWithoutSession(ConfirmCorrespondAddressControllerTest.submit, formInput)(
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
      submitWithTimeout(ConfirmCorrespondAddressControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Submitting a form to ConfirmCorrespondAddressController when NOT enrolled" should {
    "return a 303 and redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      val formInput = "contactAddressUse" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(ConfirmCorrespondAddressControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }
}
