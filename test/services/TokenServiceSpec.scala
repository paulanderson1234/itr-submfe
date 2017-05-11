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

import common.KeystoreKeys
import connectors.{KeystoreConnector, TokenConnector}
import models.throttling.TokenModel
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import play.api.libs.json.{Json, JsString, JsObject}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.{Upstream5xxResponse, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future


class TokenServiceSpec extends UnitSpec with MockitoSugar with OneAppPerTest {

  val internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5"
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1234")))

  val token = "TOK123456789"
  val tokenModel = TokenModel("TOK123456789")
  val generateTokenSucResponse = HttpResponse(OK,Some(JsObject(Seq("_id" -> JsString("TOK123456789")))))
  val generateTokenInvalidResponse = HttpResponse(OK,Some(JsObject(Seq("notToken" -> JsString("Invalid")))))
  val generateTokenFailResponse = Future.failed(Upstream5xxResponse("Error",INTERNAL_SERVER_ERROR,INTERNAL_SERVER_ERROR))
  val tokenCacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(tokenModel)))

  object TestTokenService extends TokenService{
    override val tokenConnector: TokenConnector = mock[TokenConnector]
    override val keystoreConnector: KeystoreConnector = mock[KeystoreConnector]
  }

  def mockGenerateTokenFunction(res: Future[HttpResponse], hasThrottlePassed:Option[Boolean]): Future[HttpResponse] = {
    when(TestTokenService.tokenConnector.generateTemporaryToken(Matchers.any())).thenReturn(res)

    lazy val result = TestTokenService.generateTemporaryToken
    await(result)
  }

  def mockValidateTokenFunction(tokenModel: Option[TokenModel], validated: Option[Boolean], hasThrottlePassed:Option[Boolean]): Boolean = {
    when(TestTokenService.keystoreConnector.fetchAndGetFormData[TokenModel](Matchers.eq(KeystoreKeys.throttlingToken))
    (Matchers.any(),Matchers.any())).thenReturn(tokenModel)

    when(TestTokenService.keystoreConnector.saveFormData(Matchers.eq(KeystoreKeys.throttleCheckPassed), Matchers.any())
    (Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))


    when(TestTokenService.tokenConnector.validateTemporaryToken(Matchers.any())(Matchers.any())).thenReturn(validated)
    lazy val result = TestTokenService.validateTemporaryToken
    await(result)
  }

  "The TokenService" should {
    "use the correct 'save for later' connector" in {
      TokenService.keystoreConnector shouldBe KeystoreConnector
    }
    "use the correct token connector" in {
      TokenService.tokenConnector shouldBe TokenConnector
    }
  }

  "generateTemporaryToken" should {
    "return a OK response if the call to the connector returns a valid TokenModel" in {
      val response = mockGenerateTokenFunction(generateTokenSucResponse, None)
      response.status shouldBe OK
    }
    "return INTERNAL SERVER ERROR if the call to the connector returns an invalid TokenModel" in {
      val response = mockGenerateTokenFunction(generateTokenInvalidResponse, None)
      response.status shouldBe INTERNAL_SERVER_ERROR
    }
    "return INTERNAL SERVER ERROR if the call to then connector returns a non 200 response code" in {
      val response = mockGenerateTokenFunction(generateTokenFailResponse, None)
      response.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "validateTemporaryToken" should {
    "return true if a token can be fetched from s4later and the call to validate returns Some(true)" in {
      val response = mockValidateTokenFunction(Some(tokenModel),Some(true), None)
      response shouldBe true
    }
    "return false if a token can be fetched from s4later and the call to validate returns Some(false)" in {
      val response = mockValidateTokenFunction(Some(tokenModel),Some(false), None)
      response shouldBe false
    }
    "return false if a token can be fetched from s4later and the call to validate returns None" in {
      val response = mockValidateTokenFunction(Some(tokenModel),None, None)
      response shouldBe false
    }
    "return false if a token cannot be fetched from s4later" in {
      val response = mockValidateTokenFunction(None,None, None)
      response shouldBe false
    }
    "return false if an exception occurs down stream" in {
      when(TestTokenService.keystoreConnector.fetchAndGetFormData[TokenModel](Matchers.eq(KeystoreKeys.throttlingToken))
        (Matchers.any(),Matchers.any())).thenReturn(Some(tokenModel))
      when(TestTokenService.tokenConnector.validateTemporaryToken(Matchers.any())(Matchers.any())).thenReturn(generateTokenFailResponse)
      lazy val result = TestTokenService.validateTemporaryToken
      await(result) shouldBe false
    }
  }
}
