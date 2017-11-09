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

package connectors

import auth.{MockConfig, TAVCUser, ggUser}
import controllers.helpers.{BaseSpec, FakeRequestHelper}
import models.EmailConfirmationModel
import play.api.libs.json.JsValue
import config.WSHttp
import fixtures.SubmissionFixture
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse, Upstream5xxResponse }
import uk.gov.hmrc.http.logging.SessionId

class EmailConfirmationConnectorSpec extends UnitSpec with MockitoSugar with OneAppPerSuite {

  object TestEmailConfirmationConnector extends EmailConfirmationConnector with FakeRequestHelper{
    override val serviceUrl: String = MockConfig.emailUrl
    override val http = mock[WSHttp]
    override val domain: String = MockConfig.emailDomain
    val testModel = EmailConfirmationModel(Array("test@test.com"), "test-template",
      EmailConfirmationModel.parameters("Test company", "XATESTREFNUM123456789"))
  }

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1013")))
  implicit val user: TAVCUser = TAVCUser(ggUser.allowedAuthContext, internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5")

  "EmailConfirmation Connector" should {
    "use correct http client" in {
      EmailConfirmationConnector.http shouldBe WSHttp
    }
  }

  "Calling sendEmailConfirmation" when {
    "expecting a successful response" should {
      lazy val result = TestEmailConfirmationConnector.sendEmailConfirmation(TestEmailConfirmationConnector.testModel)(headerCarrier)
      "return a Status Accepted (202) response" in {
        when(TestEmailConfirmationConnector.http.POST[JsValue, HttpResponse](Matchers.anyString(), Matchers.any(),
          Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(HttpResponse(ACCEPTED))
        await(result) match {
          case response => response.status shouldBe ACCEPTED
          case _ => fail("No response was received, when one was expected")
        }
      }
    }

    "expecting a downstream error" should {
      lazy val result = TestEmailConfirmationConnector.sendEmailConfirmation(TestEmailConfirmationConnector.testModel)(headerCarrier)
      "return a Status INTERNAL_SERVER_ERROR (500) response" in {
        when(TestEmailConfirmationConnector.http.POST[JsValue, HttpResponse](Matchers.anyString(), Matchers.any(),
          Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).
          thenReturn(Future.failed(Upstream5xxResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))
        await(result) match {
          case response => response.status shouldBe INTERNAL_SERVER_ERROR
          case _ => fail("No response was received, when one was expected")
        }
      }
    }
  }
}
