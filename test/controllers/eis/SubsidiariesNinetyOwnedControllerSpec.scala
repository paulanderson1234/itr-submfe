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
import controllers.helpers.BaseSpec
import common.Constants
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import models.SubsidiariesNinetyOwnedModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class SubsidiariesNinetyOwnedControllerSpec extends BaseSpec {

  object TestController extends SubsidiariesNinetyOwnedController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(subsidiariesNinetyOwnedModel: Option[SubsidiariesNinetyOwnedModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(subsidiariesNinetyOwnedModel))

  "SubsidiariesNinetyOwnedController" should {
    "use the correct keystore connector" in {
      SubsidiariesNinetyOwnedController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      SubsidiariesNinetyOwnedController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      SubsidiariesNinetyOwnedController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to SubsidiariesNinetyOwnedController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(subsidiariesNinetyOwnedModelYes))
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

  "Sending a valid form submission to the SubsidiariesNinetyOwnedController when authenticated and enrolled" should {
    "redirect to the how-plan-to-use-investment page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "ownNinetyPercent" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/how-plan-to-use-investment")
        }
      )
    }
  }

  "Sending an empty invalid form submission with validation errors to the SubsidiariesNinetyOwnedController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "ownNinetyPercent" -> ""
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
