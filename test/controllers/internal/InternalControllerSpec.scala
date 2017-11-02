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
import auth.MockAuthConnector
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.S4LConnector
import controllers.helpers.BaseSpec
import controllers.internal.InternalController
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.Future

class InternalControllerSpec extends BaseSpec {

  object TestController extends InternalController {
    override lazy val s4LConnector = mockS4lConnector
    override val authConnector: AuthConnector = MockAuthConnector
  }

  "InternalController" should {

    "use the correct save4later connector" in {
      InternalController.s4LConnector shouldBe S4LConnector
    }

    "use the correct auth connector" in {
      InternalController.authConnector shouldBe FrontendAuthConnector
    }
  }

  "Sending a GET request to InternalController when authenticated and enrolled" should {
    "return a Ok with the expected json body when an application is retrieved from storage" in {

      implicit val system = ActorSystem()
      implicit val materializer: Materializer = ActorMaterializer()

      when(TestController.s4LConnector.fetchAndGetFormData[Boolean](Matchers.any(), Matchers.eq(KeystoreKeys.applicationInProgress))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(false)))

      lazy val result = TestController.getApplicationInProgress.apply(authorisedFakeFrontendRequest)
      status(result) shouldBe OK
      await(bodyOf(result)) shouldBe "false"
    }

    "return Ok with false in the json body when no application is retrieved from storage" in {

      implicit val system = ActorSystem()
      implicit val materializer: Materializer = ActorMaterializer()

      when(TestController.s4LConnector.fetchAndGetFormData[Boolean](Matchers.any(), Matchers.eq(KeystoreKeys.applicationInProgress))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

      lazy val result = TestController.getApplicationInProgress.apply(authorisedFakeFrontendRequest)
      status(result) shouldBe OK
      await(bodyOf(result)) shouldBe "false"
    }
  }
}
