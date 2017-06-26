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
import common.Constants
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import controllers.seis.HasInvestmentTradeStartedController
import models.HasInvestmentTradeStartedModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class HasInvestmentTradeStartedControllerSpec extends BaseSpec {

  object TestController extends HasInvestmentTradeStartedController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "HasInvestmentTradeStartedController" should {
    "use the correct auth connector" in {
      HasInvestmentTradeStartedController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      HasInvestmentTradeStartedController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      HasInvestmentTradeStartedController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupShowMocks(hasInvestmentTradeStartedModel: Option[HasInvestmentTradeStartedModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(hasInvestmentTradeStartedModel))


  "Sending a GET request to HasInvestmentTradeStartedController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(hasInvestmentTradeStartedModelYes))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupShowMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  /*todo*/
  "Sending a valid Yes form submission to the HasInvestmentTradeStartedController when authenticated and enrolled" should {
    "redirect to the itself (TODO)" in {
      val formInput = Seq("hasInvestmentTradeStarted" -> Constants.StandardRadioButtonYesValue,
        "hasInvestmentTradeStartedDay" -> "23",
        "hasInvestmentTradeStartedMonth" -> "11",
        "hasInvestmentTradeStartedYear" -> "1993")
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.HasInvestmentTradeStartedController.show().url)
        }
      )
    }
  }

  /*todo*/
  "Sending a valid No form submission with a empty KI Model to the HasInvestmentTradeStartedController when authenticated and enrolled" should {
    "redirect to itself(todo)" in {
      val formInput = Seq(
        "hasInvestmentTradeStarted" -> Constants.StandardRadioButtonNoValue,
        "hasInvestmentTradeStartedDay" -> "",
        "hasInvestmentTradeStartedMonth" -> "",
        "hasInvestmentTradeStartedYear" -> "")
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.HasInvestmentTradeStartedController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission to the HasInvestmentTradeStartedController when authenticated and enrolled" should {
    "redirect respond with BADREQUEST" in {
      val formInput = Seq(
        "hasInvestmentTradeStarted" -> "",
        "hasInvestmentTradeStartedDay" -> "",
        "hasInvestmentTradeStartedMonth" -> "",
        "hasInvestmentTradeStartedYear" -> "")
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
