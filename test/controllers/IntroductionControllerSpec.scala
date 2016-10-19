/*
 * Copyright 2016 HM Revenue & Customs
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

package controllers

import connectors.S4LConnector
import controllers.helpers.ControllerSpec
import play.api.test.Helpers._

class IntroductionControllerSpec extends ControllerSpec {

  object TestController extends IntroductionController {
    val s4lConnector: S4LConnector = mockS4lConnector
  }

  "IntroductionController" should {
    "Use the correct keystore connector" in {
      IntroductionController.s4lConnector shouldBe S4LConnector
    }
  }

  "IntroductionController.show" should {
    "return 200" in {
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "IntroductionController.submit" should {
    "return a 303" in {
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit())(
        result => {
          status(result) shouldBe SEE_OTHER
        }
      )
    }
  }

  "IntroductionController.restart" should {
    "return a 303" in {
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.restart())(
        result => {
          status(result) shouldBe SEE_OTHER
        }
      )
    }
  }
}