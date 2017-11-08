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
import connectors.{KeystoreConnector, ThrottleConnector}
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, Upstream5xxResponse }
import uk.gov.hmrc.http.logging.SessionId


class ThrottleServiceSpec extends UnitSpec with MockitoSugar with OneAppPerTest  {

  val internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5"
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1234")))
  val generateTokenFailResponse = Future.failed(Upstream5xxResponse("Error",INTERNAL_SERVER_ERROR,INTERNAL_SERVER_ERROR))

  val mockKeyStoreConnector =  mock[KeystoreConnector]
  val mockThrottleConnector =  mock[ThrottleConnector]

  object TestThrottleService extends ThrottleService{
    override val throttleConnector: ThrottleConnector = mockThrottleConnector
    override val keystoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  "The ThrottleService" should {
    "use the correct throttle connector" in {
      ThrottleService.throttleConnector shouldBe ThrottleConnector
    }
  }

  def setupThrottleCheck(hasThrottlePassed:Option[Boolean]) {

    when(TestThrottleService.keystoreConnector.saveFormData(Matchers.eq(KeystoreKeys.throttleCheckPassed),
      Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

    when(TestThrottleService.keystoreConnector.fetchAndGetFormData[Boolean](Matchers.eq(KeystoreKeys.throttleCheckPassed))
      (Matchers.any(),Matchers.any())).thenReturn(if(hasThrottlePassed.isDefined)
      Future.successful(Option(hasThrottlePassed.getOrElse(false))) else Future.successful(None))

  }

  "checkUserAccess" should {
    "return true if the call to validate returns Some(true) if not session throttle flag" in {
      lazy val response = TestThrottleService.checkUserAccess
      setupThrottleCheck(None)
      when(TestThrottleService.throttleConnector.checkUserAccess()(Matchers.any())).thenReturn(Some(true))
      await(response) shouldBe true
    }

    "return true if the call to validate finds an existing throttle check in session as true and does not call the throttle check" in {
      lazy val response = TestThrottleService.checkUserAccess
      setupThrottleCheck(Some(true))
      // no need to mock TestThrottleService.throttleConnector.checkUserAcces as it shouldn't be called
      await(response) shouldBe true
    }
    "return true if the call to validate does not find an existing throttle check in session as None and calls the throttle check" in {
      lazy val response = TestThrottleService.checkUserAccess
      setupThrottleCheck(None)
      when(TestThrottleService.throttleConnector.checkUserAccess()(Matchers.any())).thenReturn(Some(true))
      await(response) shouldBe true
    }
    "return true if the call to validate finds an existing throttle check in session as false and calls the throttle check" in {

      lazy val response = TestThrottleService.checkUserAccess
      when(TestThrottleService.throttleConnector.checkUserAccess()(Matchers.any())).thenReturn(Some(true))
      setupThrottleCheck(Some(false))

      await(response) shouldBe true
    }
    "return false if the call to validate returns Some(false)" in {
      lazy val response = TestThrottleService.checkUserAccess

      setupThrottleCheck(None)
      when(TestThrottleService.throttleConnector.checkUserAccess()(Matchers.any())).thenReturn(Some(false))
      await(response) shouldBe false
    }
    "return false if the call to validate returns None" in {
      lazy val response = TestThrottleService.checkUserAccess
      setupThrottleCheck(None)

      when(TestThrottleService.throttleConnector.checkUserAccess()(Matchers.any())).thenReturn(None)
      await(response) shouldBe false
    }
    "return false if an exception occurs down stream" in {
      lazy val response = TestThrottleService.checkUserAccess

      setupThrottleCheck(None)

      when(TestThrottleService.throttleConnector.checkUserAccess()(Matchers.any())).thenReturn(generateTokenFailResponse)
      await(response) shouldBe false
    }
  }
}