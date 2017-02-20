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

package controllers.eis

import auth.{MockAuthConnector, MockConfig}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.AddressModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class ContactAddressControllerSpec extends BaseSpec {

  object TestController extends ContactAddressController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
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

  def setupMocks(addressModel : Option[AddressModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(addressModel))

  "Sending a GET request to ContactAddressController when authenticated and enrolled" should {
    "return a 200 OK when something is fetched from keystore" in {
      setupMocks(Some(addressModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid form submit to the ContactAddressController when authenticated and enrolled" should {
    "redirect to the Contact Details Subscription Controller page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput =
        Seq("addressline1" -> "Line 1",
          "addressline2" -> "Line 2",
          "addressline3" -> "Line 3",
          "addressline4" -> "line 4",
          "postcode" -> "AA1 1AA",
          "countryCode" -> "GB")

      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SupportingDocumentsController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ContactAddressController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = Seq("addressLine1" -> "", "addressLine1" -> "", "addressLine3" -> "Line3",
        "addressLine4" -> "Line4", "postCode" -> "AA1 1AA", "countryCode" -> "GB")
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
