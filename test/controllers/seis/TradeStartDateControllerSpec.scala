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
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class TradeStartDateControllerSpec extends BaseSpec {

  object TestController extends TradeStartDateController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val submissionConnector = mockSubmissionConnector
  }

  "TradeStartDateController" should {
    "use the correct auth connector" in {
      TradeStartDateController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      TradeStartDateController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      TradeStartDateController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct submission Connector" in {
      TradeStartDateController.submissionConnector shouldBe SubmissionConnector
    }
  }

  def setupShowMocks(tradeStartDateModel: Option[TradeStartDateModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(tradeStartDateModel))

  def setupSubmitMocks(result: Option[Boolean]): Unit = {
    when(mockSubmissionConnector.validateTradeStartDateCondition(Matchers.any(),Matchers.any(),Matchers.any())(Matchers.any())).thenReturn(Future.successful(result))
  }

  "Sending a GET request to TradeStartDateController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(tradeStartDateModelYes))
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

  "Sending a valid Yes form submission to the TradeStartDateController when authenticated and enrolled" should {
    "redirect to the Is This First Trade Your Company Has Carried Out page if the Trade start date condition is met" in {
      val formInput = Seq(
        "hasTradeStartDate" -> Constants.StandardRadioButtonYesValue,
        "tradeStartDay" -> "23",
        "tradeStartMonth" -> "11",
        "tradeStartYear" -> "2015")
      setupSubmitMocks(Some(true))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.IsFirstTradeController.show().url)
        }
      )
    }
  }

  "Sending a valid Yes form submission to the TradeStartDateController when authenticated and enrolled" should {
    "redirect to the Trade Start Date Error page if the Trade Start date condition is not met" in {
      val formInput = Seq(
        "hasTradeStartDate" -> Constants.StandardRadioButtonYesValue,
        "tradeStartDay" -> "23",
        "tradeStartMonth" -> "11",
        "tradeStartYear" -> "2014")
      setupSubmitMocks(Some(false))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.TradeStartDateErrorController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the TradeStartDateController when authenticated and enrolled" should {
    "redirect to the Is This First Trade Your Company Has Carried Out page" in {
      val formInput = Seq(
        "hasTradeStartDate" -> Constants.StandardRadioButtonNoValue)
      setupSubmitMocks(Some(true))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.IsFirstTradeController.show().url)
        }
      )
    }
  }
  "Sending a valid Yes form submission to the TradeStartDateController when authenticated and enrolled" should {
      "redirect to the INTERNAL_SERVER_ERROR page as the API call has failed" in {
        val formInput = Seq(
          "hasTradeStartDate" -> Constants.StandardRadioButtonYesValue,
          "tradeStartDay" -> "23",
          "tradeStartMonth" -> "11",
          "tradeStartYear" -> "2014")
      setupSubmitMocks(None)
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }

}
