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

import auth.{ggUser, TAVCUser}
import config.FrontendAppConfig
import controllers.helpers.FakeRequestHelper
import fixtures.SubmissionFixture
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec

class AttachmentsFrontEndConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneAppPerSuite with SubmissionFixture {

  object TargetAttachmentsFrontEndConnector extends AttachmentsFrontEndConnector with FrontendController with FakeRequestHelper with ServicesConfig {
    override val internalAttachmentsUrl = FrontendAppConfig.internalAttachmentsUrl
    override val http = mock[WSHttp]
  }

  val internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5"

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1234")))

  implicit val user: TAVCUser = TAVCUser(ggUser.allowedAuthContext, internalId)
  
  val validTavcReference = "XATAVC000123456"

  val successResponse = HttpResponse(Status.OK)

  val failedResponse = HttpResponse(Status.BAD_REQUEST)

  val envelopeId: Option[String] = Some("000000000000000000000000")


  "Calling closeEnvelope" when {

    "expecting a successful response" should {
      "return a Status OK (200) response" in {
        when(TargetAttachmentsFrontEndConnector.http.POSTEmpty[HttpResponse](Matchers.eq(s"${TargetAttachmentsFrontEndConnector.internalAttachmentsUrl}" +
          s"/internal/$validTavcReference/${envelopeId.get}/${user.internalId}/close-envelope"))
          (Matchers.any(),Matchers.any())).thenReturn(successResponse)
        val result = TargetAttachmentsFrontEndConnector.closeEnvelope(validTavcReference, envelopeId.get)
        await(result) match {
          case response => response shouldBe successResponse
          case _ => fail("No response was received, when one was expected")
        }
      }

    }

    "expecting a non-successful response" should {
      "return a Status BAD_REQUEST (400) response" in {
        when(TargetAttachmentsFrontEndConnector.http.POSTEmpty[HttpResponse](Matchers.eq(s"${TargetAttachmentsFrontEndConnector.internalAttachmentsUrl}" +
          s"/internal/$validTavcReference/${envelopeId.get}/${user.internalId}/close-envelope"))
          (Matchers.any(),Matchers.any())).thenReturn(failedResponse)
        val result = TargetAttachmentsFrontEndConnector.closeEnvelope(validTavcReference, envelopeId.get)
        await(result) match {
          case response => response shouldBe failedResponse
          case _ => fail("No response was received, when one was expected")
        }
      }
    }
  }
}
