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

import connectors.{ThrottleConnector}
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.{Upstream5xxResponse, HeaderCarrier}
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import play.api.test.Helpers._
import scala.concurrent.Future


class ThrottleServiceSpec extends UnitSpec with MockitoSugar with OneAppPerTest {

  val internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5"
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1234")))
  val generateTokenFailResponse = Future.failed(Upstream5xxResponse("Error",INTERNAL_SERVER_ERROR,INTERNAL_SERVER_ERROR))

  object TestThrottleService extends ThrottleService{
    override val throttleConnector: ThrottleConnector = mock[ThrottleConnector]
  }


  "The ThrottleService" should {
    "use the correct throttle connector" in {
      ThrottleService.throttleConnector shouldBe ThrottleConnector
    }
  }


  "checkUserAccess" should {
    "return true if the call to validate returns Some(true)" in {
      lazy val response = TestThrottleService.checkUserAccess
      when(TestThrottleService.throttleConnector.checkUserAccess()(Matchers.any())).thenReturn(Some(true))
      await(response) shouldBe true
    }
    "return false if the call to validate returns Some(false)" in {
      lazy val response = TestThrottleService.checkUserAccess
      when(TestThrottleService.throttleConnector.checkUserAccess()(Matchers.any())).thenReturn(Some(false))
      await(response) shouldBe false
    }
    "return false if the call to validate returns None" in {
      lazy val response = TestThrottleService.checkUserAccess
      when(TestThrottleService.throttleConnector.checkUserAccess()(Matchers.any())).thenReturn(None)
      await(response) shouldBe false
    }
    "return false if an exception occurs down stream" in {
      lazy val response = TestThrottleService.checkUserAccess
      when(TestThrottleService.throttleConnector.checkUserAccess()(Matchers.any())).thenReturn(generateTokenFailResponse)
      await(response) shouldBe false
    }
  }
}