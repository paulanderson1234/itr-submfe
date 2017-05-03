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
import org.mockito.Matchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.{HeaderCarrier}
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class ThrottleConnectorSpec extends UnitSpec with MockitoSugar with OneAppPerTest {

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1234")))

  object TestThrottleConnector extends ThrottleConnector with FrontendController {
    override val serviceUrl = MockConfig.submissionUrl
    override val http = mock[WSHttp]
  }



  def setupMockedCheckUserAccess(data: Option[Boolean]): OngoingStubbing[Future[Option[Boolean]]] = {
    when(TestThrottleConnector.http.GET[Option[Boolean]](
      Matchers.eq(s"${TestThrottleConnector.serviceUrl}/investment-tax-relief/throttle/check-user-access"))
      (Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))
  }

  "ThrottleConnector" should {
    "use correct http client" in {
      ThrottleConnector.http shouldBe WSHttp
    }
  }


  "Calling checkUserAccess" when {

    "expecting a successful response" should {

      "return a Some(true) response" in {
        lazy val result = TestThrottleConnector.checkUserAccess()
        setupMockedCheckUserAccess(Some(true))
        await(result) match {
          case response => {
            response should not be empty
            response shouldBe Some(true)
          }
        }
      }

      "return a Some(false) response" in {
        lazy val result = TestThrottleConnector.checkUserAccess()
        setupMockedCheckUserAccess(Some(false))
        await(result) match {
          case response => {
            response should not be empty
            response shouldBe Some(false)
          }
        }
      }

    }
  }
}