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

import auth.MockConfig
import config.WSHttp
import models.throttling.TokenModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.SessionId

class TokenConnectorSpec extends UnitSpec with MockitoSugar with OneAppPerTest {

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1234")))

  val tokenId = "123456789"


  val successResponse = HttpResponse(Status.OK, responseJson = Some(Json.parse(
    """
      |{
      |    "id": "TOK123456789"
      |}
    """.stripMargin
  )))

  val failedResponse = HttpResponse(Status.INTERNAL_SERVER_ERROR)

  trait mockHttp extends HttpGet with HttpPost
  object TestTokenConnector extends TokenConnector with FrontendController {
    override val serviceUrl = MockConfig.submissionUrl
    override val http = mock[mockHttp]
  }

  def setupMockedGenerateTempTokenResponse(data: HttpResponse): OngoingStubbing[Future[HttpResponse]] = {
    when(TestTokenConnector.http.POSTEmpty[HttpResponse](
      Matchers.eq(s"${TestTokenConnector.serviceUrl}/investment-tax-relief/token/generate-temporary-token"))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))
  }

  def setupMockedValidateTempTokenResponse(data: Option[Boolean]): OngoingStubbing[Future[Option[Boolean]]] = {
    when(TestTokenConnector.http.GET[Option[Boolean]](
      Matchers.eq(s"${TestTokenConnector.serviceUrl}/investment-tax-relief/token/validate-temporary-token/$tokenId"))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))
  }

  "TokenConnector" should {
    "use correct http client" in {
      TokenConnector.http shouldBe WSHttp
    }
  }

  "Calling generateTemporaryToken" when {

    "expecting a successful response" should {
      lazy val result = TestTokenConnector.generateTemporaryToken

      "return a Status OK (200) response" in {
        setupMockedGenerateTempTokenResponse(successResponse)
        await(result) match {
          case response => response.status shouldBe Status.OK
        }
      }

      "Have a successful Json Body response" in {
        setupMockedGenerateTempTokenResponse(successResponse)
        await(result) match {
          case response => response.json shouldBe successResponse.json
        }
      }
    }

    "expecting a non-successful response" should {
      lazy val result = TestTokenConnector.generateTemporaryToken

      "return a Status INTERNAL_SERVER_ERROR (500) response" in {
        setupMockedGenerateTempTokenResponse(failedResponse)
        await(result) match {
          case response => response.status shouldBe Status.INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "Calling validateTemporaryToken" when {

    "expecting a successful response" should {

      "return a Some(true) response" in {
        lazy val result = TestTokenConnector.validateTemporaryToken(Some(tokenId))
        setupMockedValidateTempTokenResponse(Some(true))
        await(result) match {
          case response => {
            response should not be empty
            response shouldBe Some(true)
          }
        }
      }

      "return a Some(false) response" in {
        lazy val result = TestTokenConnector.validateTemporaryToken(Some(tokenId))
        setupMockedValidateTempTokenResponse(Some(false))
        await(result) match {
          case response => {
            response should not be empty
            response shouldBe Some(false)
          }
        }
      }
    }

    "with an empty token model" should {
      lazy val result = TestTokenConnector.validateTemporaryToken(None)

      "return false" in {
        setupMockedValidateTempTokenResponse(None)
        await(result) match {
          case response => response.get shouldBe false
        }
      }
    }
  }
}
