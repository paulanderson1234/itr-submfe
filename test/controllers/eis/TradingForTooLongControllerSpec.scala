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
import connectors.EnrolmentConnector
<<<<<<< HEAD:test/controllers/eis/TradingForTooLongControllerSpec.scala
import controllers.helpers.ControllerSpec
=======
import helpers.BaseSpec
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/TradingForTooLongControllerSpec.scala
import play.api.test.Helpers._


class TradingForTooLongControllerSpec extends BaseSpec {

  object TestController extends TradingForTooLongController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "TradingForTooLongController" should {
    "use the correct auth connector" in {
      TradingForTooLongController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      TradingForTooLongController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to TradingForTooLongController when authenticated and enrolled" should {
    "return a 200 OK Swhen something is fetched from keystore" in {
       mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 OK when nothing is fetched using keystore" in {
   
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

<<<<<<< HEAD:test/controllers/eis/TradingForTooLongControllerSpec.scala

  "Sending a request with no session to TradingForTooLongController" should {
    "return a 303" in {
      status(TestController.show(fakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(TestController.show(fakeRequest)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
      }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending an Unauthenticated request with a session to TradingForTooLongController" should {
    "return a 303" in {
      status(TestController.show(fakeRequestWithSession)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(TestController.show(fakeRequestWithSession)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
      }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending a timed-out request to TradingForTooLongController" should {

    "return a 303 in" in {
      status(TestController.show(timedOutFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to timeout page" in {
      redirectLocation(TestController.show(timedOutFakeRequest)) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
    }
  }

  "Sending a request to TradingForTooLongController when NOT enrolled" should {
    "return a 303 in" in {
      mockNotEnrolledRequest()
      status(TestController.show(authorisedFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to the Subscription Service" in {
      mockNotEnrolledRequest()
      redirectLocation(TestController.show(authorisedFakeRequest)) shouldBe Some(FrontendAppConfig.subscriptionUrl)
    }
  }
=======
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/TradingForTooLongControllerSpec.scala
}
