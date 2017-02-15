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
import common.Constants
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class UsedInvestmentReasonBeforeControllerSpec extends BaseSpec {

  object TestController extends UsedInvestmentReasonBeforeController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(usedInvestmentReasonBeforeModel: Option[UsedInvestmentReasonBeforeModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(usedInvestmentReasonBeforeModel))

  "UsedInvestmentReasonBeforeController" should {
    "use the correct keystore connector" in {
      UsedInvestmentReasonBeforeController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      UsedInvestmentReasonBeforeController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      UsedInvestmentReasonBeforeController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to UsedInvestmentReasonBeforeController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(usedInvestmentReasonBeforeModelYes))
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

  "Sending a valid 'Yes' form submit to the UsedInvestmentReasonBeforeController when authenticated and enrolled" should {
    "redirect to the subsidiaries page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "usedInvestmentReasonBefore" -> Constants.StandardRadioButtonYesValue)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.PreviousBeforeDOFCSController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the UsedInvestmentReasonBeforeController when authenticated and enrolled" should {
    "redirect the ten year plan page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "usedInvestmentReasonBefore" -> Constants.StandardRadioButtonNoValue)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.NewGeographicalMarketController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the UsedInvestmentReasonBeforeController" should {
    "redirect to itself" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "usedInvestmentReasonBefore" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
