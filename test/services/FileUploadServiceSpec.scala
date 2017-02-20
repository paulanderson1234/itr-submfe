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

package services

import auth.{ggUser, TAVCUser}
import connectors.{AttachmentsFrontEndConnector}
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.test.UnitSpec
import data.SubscriptionTestData._
import scala.concurrent.ExecutionContext.Implicits.global
import org.mockito.Mockito._
import play.api.test.Helpers._
import scala.concurrent.Future


class FileUploadServiceSpec extends UnitSpec with MockitoSugar with OneAppPerTest {

  object TargetFileUploadService extends FileUploadService with FrontendController {
    override val attachmentsFrontEndConnector = mock[AttachmentsFrontEndConnector]
    override val getUploadFeatureEnabled = true
  }

  val internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5"

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1234")))
  implicit val user: TAVCUser = TAVCUser(ggUser.allowedAuthContext, internalId)

  val envelopeId: Option[String] = Some("000000000000000000000000")


  "Calling closeEnvelope" when {

    lazy val result = TargetFileUploadService.closeEnvelope(envelopeId.get, validTavcReference)
    lazy val response = await(result)

     "return the response code if any response code is received" in {
       when(TargetFileUploadService.attachmentsFrontEndConnector.closeEnvelope(Matchers.any(), Matchers.any())(Matchers.any(),Matchers.any())).
         thenReturn(Future(HttpResponse(CREATED)))
        response.status shouldBe CREATED
      }
    }
}
