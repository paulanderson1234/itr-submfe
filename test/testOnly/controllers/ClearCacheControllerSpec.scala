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

import auth.MockAuthConnector
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future
import uk.gov.hmrc.http.HttpResponse

class ClearCacheControllerSpec extends BaseSpec {

  object TestController extends ClearCacheController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "ClearCacheController" should {
    "Use the correct s4l connector" in {
      ClearCacheController.s4lConnector shouldBe S4LConnector
    }
    "Use the correct auth connector" in {
      ClearCacheController.authConnector shouldBe FrontendAuthConnector
    }
    "Use the correct enrolment connector" in {
      ClearCacheController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "Use the correct app config" in {
      ClearCacheController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  def setupMocks(result: Int): Unit =
    when(mockS4lConnector.clearCache()(Matchers.any(),Matchers.any())).thenReturn(Future.successful(HttpResponse(result)))

  "ClearCacheController.clearCache" when {

    "Called as an authorised and enrolled user and s4lConnector.clearCache returns a NO_CONTENT" should {

      "Return OK" in {
        mockEnrolledRequest()
        setupMocks(NO_CONTENT)
        showWithSessionAndAuth(TestController.clearCache())(
          result => status(result) shouldBe OK
        )
      }

      "Output a string response" in {
        mockEnrolledRequest()
        setupMocks(NO_CONTENT)
        showWithSessionAndAuth(TestController.clearCache())(
          result => contentAsString(result) shouldBe "Successfully cleared cache"
        )
      }

    }

    "Called as an authorised and enrolled user and s4lConnector.clearCache returns a BAD_REQUEST" should {

      "Return OK" in {
        mockEnrolledRequest()
        setupMocks(BAD_REQUEST)
        showWithSessionAndAuth(TestController.clearCache())(
          result => status(result) shouldBe BAD_REQUEST
        )
      }

      "Output a string response" in {
        mockEnrolledRequest()
        setupMocks(BAD_REQUEST)
        showWithSessionAndAuth(TestController.clearCache())(
          result => contentAsString(result) shouldBe "Failed to clear cache"
        )
      }

    }

  }

}
