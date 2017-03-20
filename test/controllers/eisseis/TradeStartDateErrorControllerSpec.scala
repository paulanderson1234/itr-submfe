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

package controllers.eisseis

import auth.{MockAuthConnector, MockConfig}
import common.Constants
import config.FrontendAuthConnector
import connectors.EnrolmentConnector
import controllers.helpers.BaseSpec
import play.api.test.Helpers._


class TradeStartDateErrorControllerSpec extends BaseSpec {

  object TestController extends TradeStartDateErrorController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "TradeStartDateErrorController" should {
    "use the correct auth connector" in {
      TradeStartDateErrorController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      TradeStartDateErrorController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to TradeStartDateErrorController when authenticated and enrolled" should {
    "return a 200 OK when something is fetched from keystore" in {
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 OK when nothing is fetched using keystore" in {

      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending submission to TradeStartDateErrorController when authenticated and enrolled" should {
    "redirect to Is this the first trade page" in {
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eisseis.routes.IsFirstTradeController.show().url)
        }
      )
    }
  }

//  INVALID TEST
//  "Posting to the LifetimeAllowanceExceededController when authenticated and enrolled" should {
//    "redirect to 'Proposed investment' page" in {
//      mockEnrolledRequest(eisSeisSchemeTypesModel)
//      submitWithSessionAndAuth(TestController.submit)(
//        result => {
//          status(result) shouldBe SEE_OTHER
//          redirectLocation(result) shouldBe Some(routes.CommercialSaleController.show().url)
//        }
//      )
//    }
//  }

}
