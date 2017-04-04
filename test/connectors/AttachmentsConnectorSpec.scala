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

/**
  * Copyright 2016 HM Revenue & Customs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package connectors

import auth.{MockConfig, TAVCUser, ggUser}
import controllers.helpers.{BaseSpec, FakeRequestHelper}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.logging.SessionId
import config.WSHttp
import fixtures.SubmissionFixture
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.Helpers.OK
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class AttachmentsConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneAppPerSuite with SubmissionFixture {

  object TestAttachmentsConnector extends AttachmentsConnector with FakeRequestHelper{
    override val serviceUrl: String = MockConfig.attachmentsServiceUrl
    override val http = mock[WSHttp]
  }

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1013")))
  implicit val user: TAVCUser = TAVCUser(ggUser.allowedAuthContext, internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5")

  val envelopeId = "11111111111111"

  "AttachmentsConnector" should {
    "use correct http client" in {
      AttachmentsConnector.http shouldBe WSHttp
    }
  }

  "Calling getEnvelopeStatus" when {
    "expecting a successful response" should {
      lazy val result = TestAttachmentsConnector.getEnvelopeStatus(envelopeId)(headerCarrier)
      "return a Status OK (200) response" in {
        when(TestAttachmentsConnector.http.GET[HttpResponse](
          Matchers.eq(s"${TestAttachmentsConnector.serviceUrl}/investment-tax-relief-attachments/file-upload/envelope/$envelopeId/get-envelope-status"))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(OK)))
        await(result) match {
          case response => response.status shouldBe OK
          case _ => fail("No response was received, when one was expected")
        }
      }
    }
  }
}
