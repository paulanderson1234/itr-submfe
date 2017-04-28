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

import auth.{MockAuthConnector, MockConfig}
import config.FrontendAuthConnector
import connectors.EnrolmentConnector
import controllers.helpers.BaseSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers.{redirectLocation, _}
import play.api.test.Helpers._

class OurServiceChangeControllerSpec extends BaseSpec {

  object TestController extends OurServiceChangeController {
//    override lazy val applicationConfig = MockConfig

  }

  "Sending a GET request to OurServiceChangeController" should {
    "return a 200 OK" in {
//      setupMocks()
      showWithoutSession(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "POST to the OurServiceChangeController" should {
    "redirect to Who Can Use New Service page" in {
//      setupMocks()
        submitWithoutSession(TestController.submit){
        result => status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.WhoCanUseNewServiceController.show().url)
      }
    }
  }







}
