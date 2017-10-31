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

package controllers.hubGuidance

import auth.{MockAuthConnector, MockConfig}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.CheckDocumentsController
import controllers.helpers.BaseSpec
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class HubGuidanceFeedbackControllerSpec extends BaseSpec {

  trait TestController extends HubGuidanceFeedbackController {
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  object TestControllerCombined extends TestController {
    override lazy val applicationConfig = MockConfig
  }

  object TestControllerSingle extends TestController {
    override lazy val applicationConfig = MockConfig
  }

  object TestControllerEIS extends TestController {
    override lazy val applicationConfig = MockConfig
  }

  "HubGuidanceFeedbackController" should {
    "use the correct auth connector" in {
      CheckDocumentsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      CheckDocumentsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      CheckDocumentsController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct registration service" in {
      CheckDocumentsController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to HubGuidanceFeedbackController" should {
    "return a 200 OK" in {
      mockEnrolledRequest()
      showWithSessionAndAuth(TestControllerCombined.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Post to the HubGuidanceFeedbackController when authenticated and enrolled" should {
    "redirect to 'scheme selections' page" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map())))
      mockEnrolledRequest(None)
      submitWithSessionAndAuth(TestControllerCombined.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.schemeSelection.routes.SchemeSelectionController.show().url)
        }
      )
    }
  }
}
