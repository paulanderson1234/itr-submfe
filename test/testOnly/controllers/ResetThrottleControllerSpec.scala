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

package testOnly.controllers

import controllers.helpers.BaseSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import testOnly.connectors.ResetThrottleConnector
import scala.concurrent.Future
import uk.gov.hmrc.http.HttpResponse

class ResetThrottleControllerSpec extends BaseSpec {

  val mockResetThrottleConnector = mock[ResetThrottleConnector]

  object TestController extends ResetThrottleController {
    override lazy val resetThrottleConnector = mockResetThrottleConnector
  }

  "resetThrottleController" should {
    "Use the correct s4l connector" in {
      TestController.resetThrottleConnector shouldBe mockResetThrottleConnector
    }
  }

  def setupMocks(result: Int) : Unit = {
    when(TestController.resetThrottleConnector.resetThrottle()
    (Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(result)))
  }

  "resetThrottleController.resetThrottle" when {
    "returns an OK " should {
      "Return OK" in {
        setupMocks(OK)
        showWithSessionAndAuth(TestController.resetThrottle())(
          result => status(result) shouldBe OK
        )
      }

      "Output a string response" in {
        setupMocks(OK)
        showWithSessionAndAuth(TestController.resetThrottle())(
          result => contentAsString(result) shouldBe "Successfully reset throttle"
        )
      }

    }

    "returns a BAD_REQUEST" should {
      "Return BadRequest" in {
        setupMocks(BAD_REQUEST)
        showWithSessionAndAuth(TestController.resetThrottle())(
          result => status(result) shouldBe BAD_REQUEST
        )
      }

      "Output a string response" in {
        setupMocks(BAD_REQUEST)
        showWithSessionAndAuth(TestController.resetThrottle())(
          result => contentAsString(result) shouldBe "Failed to reset throttle"
        )
      }

    }

  }

}
