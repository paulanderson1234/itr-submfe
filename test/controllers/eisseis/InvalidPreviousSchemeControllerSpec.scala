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
import config.FrontendAuthConnector
import connectors.EnrolmentConnector
import controllers.helpers.BaseSpec
import play.api.test.Helpers._

class InvalidPreviousSchemeControllerSpec extends BaseSpec {

  object TestController extends InvalidPreviousSchemeController{
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "InvalidPreviousSchemeController" should {
    "use the correct auth connector" in {
      InvalidPreviousSchemeController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      InvalidPreviousSchemeController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to InvalidPreviousSchemeController when authenticated and enrolled" should {
    "return a 200" in {
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

}

