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

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import auth.{MockAuthConnector, MockConfig}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import controllers.internal.InternalController
import play.api.test.Helpers._

class InternalControllerSpec extends BaseSpec {

  object TestController extends InternalController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "InternalController" should {
    "use the correct auth connector" in {
      InternalController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      InternalController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      InternalController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to InternalController when authenticated and enrolled" should {
    "return a Ok with the expected json body" in {

      implicit val system = ActorSystem()
      implicit val materializer: Materializer = ActorMaterializer()

      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.getApplicationInProgress(internalId))(
        result => {
          status(result) shouldBe OK
          await(jsonBodyOf(result)).toString() shouldBe "false"
        }
      )
    }
  }
}
