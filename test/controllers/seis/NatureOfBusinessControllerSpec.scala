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

package controllers.seis

import auth.{MockAuthConnector, MockConfig}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class NatureOfBusinessControllerSpec extends BaseSpec {

  object TestController extends NatureOfBusinessController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "NatureOfBusinessController" should {
    "use the correct keystore connector" in {
      NatureOfBusinessController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      NatureOfBusinessController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      NatureOfBusinessController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(natureOfBusinessModel: Option[NatureOfBusinessModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(natureOfBusinessModel))

  "Sending a GET request to NatureOfBusinessController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(natureOfBusinessModel))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid form submit to the NatureOfBusinessController when auththenticated and enrolled" should {
    "redirect to the date of incorporation page" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "natureofbusiness" -> "some text so it's valid"

      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the NatureOfBusinessController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "natureofbusiness" -> ""

      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
