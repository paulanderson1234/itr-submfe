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

import controllers.helpers.BaseSpec
import play.api.test.Helpers._

class WhocanUsenewServiceControllerSpec extends BaseSpec {

  object TestController extends WhoCanUseNewServiceController {
  }

  "Sending a GET request to WhoCanUseNewServiceController" should {
    "return a 200 OK" in {
      showWithoutSession(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  //TODO: change test when new page is navigated to
  "POST to the OurServiceChangeController" should {
    "redirect to Who Can Use New Service page" in {
      submitWithoutSession(TestController.submit){
        result => status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.WhoCanUseNewServiceController.show().url)
      }
    }
  }

}
