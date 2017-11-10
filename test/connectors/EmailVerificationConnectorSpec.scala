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
import controllers.helpers.FakeRequestHelper
import models.EmailVerificationRequest
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status.{CONFLICT, CREATED, OK}
import uk.gov.hmrc.play.test.UnitSpec
import utils.WSHTTPMock

import scala.concurrent.Future
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

class EmailVerificationConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneAppPerSuite with WSHTTPMock {

  trait mockHttp extends HttpGet with HttpPost with HttpPut with HttpDelete
  object TestEmailVerificationConnector extends EmailVerificationConnector with FakeRequestHelper{
    override val checkVerifiedEmailURL: String = MockConfig.checkVerifiedEmailURL
    override val sendVerificationEmailURL: String = MockConfig.sendVerificationEmailURL
    override val http = mock[mockHttp]
  }

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1013")))
  implicit val user: TAVCUser = TAVCUser(ggUser.allowedAuthContext, internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5")

  val verifiedEmail = "verified@email.com"

  val verificationRequest = EmailVerificationRequest(
    "testEmail",
    "register_your_company_verification _email",
    Map(),
    "linkExpiry",
    "aContinueURL"
  )

  "requestVerificationEmail" should {

    "return true with valid email request" in {

      mockHttpPOST(TestEmailVerificationConnector.sendVerificationEmailURL, HttpResponse(CREATED))

      await(TestEmailVerificationConnector.requestVerificationEmail(verificationRequest)) shouldBe true
    }

    "return false with invalid email or verified email request" in {

      mockHttpPOST(TestEmailVerificationConnector.sendVerificationEmailURL, HttpResponse(CONFLICT))

      await(TestEmailVerificationConnector.requestVerificationEmail(verificationRequest)) shouldBe false
    }
  }

  "checkVerifiedEmail" should {

    "return true when passed an email that has been verified" in {
      mockHttpGet(TestEmailVerificationConnector.checkVerifiedEmailURL, HttpResponse(OK))

      await(TestEmailVerificationConnector.checkVerifiedEmail(verifiedEmail)) shouldBe true
    }

    "return false when passed an email either not valid or not verified yet" in {
      when(mockWSHttp.GET[HttpResponse](Matchers.anyString())(Matchers.any(), Matchers.any[HeaderCarrier](), Matchers.any()))
        .thenReturn(Future.failed(new NotFoundException("error")))

      await(TestEmailVerificationConnector.checkVerifiedEmail(verifiedEmail)) shouldBe false
    }
  }
}
