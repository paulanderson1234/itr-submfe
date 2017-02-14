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
import connectors.EnrolmentConnector
import controllers.helpers.BaseSpec
import play.api.test.Helpers._

class PreviousInvestmentsAllowanceExceededControllerSpec extends BaseSpec {

  object TestController extends PreviousInvestmentsAllowanceExceededController{
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "PreviousInvestmentsAllowanceExceededController" should {
    "use the correct auth connector" in {
      PreviousInvestmentsAllowanceExceededController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      PreviousInvestmentsAllowanceExceededController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to PreviousInvestmentsAllowanceExceededController when authenticated and enrolled" should {
    "return a 200" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

}
