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

package controllers.throttlingGuidance

import common.Constants
import connectors.KeystoreConnector
import controllers.helpers.BaseSpec
import play.api.test.Helpers._

class FirstTimeUsingServiceControllerSpec extends BaseSpec {

  object TestController extends FirstTimeUsingServiceController {
    override lazy val keystoreConnector = mock[KeystoreConnector]
  }

  "Sending a GET request to FirstTimeUsingServiceController" should {
    "return a 200 OK" in {
      showWithoutSession(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "POST to the FirstTimeUsingServiceController" should {

    "redirect to Hub page" in {
      val formInput = "isFirstTimeUsingService" -> Constants.StandardRadioButtonNoValue
      submitWithoutSession(TestController.submit, formInput){
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
          }
      }
    }

    //TODO: change test when new page is navigated to
    "redirect to same page" in {
      val formInput = "isFirstTimeUsingService" -> Constants.StandardRadioButtonYesValue
      submitWithoutSession(TestController.submit, formInput){
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.FirstTimeUsingServiceController.show().url)
        }
      }
    }
  }
}
