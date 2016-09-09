/*
 * Copyright 2016 HM Revenue & Customs
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

import java.util.UUID

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, HttpPut}
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SubmissionConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockHttp = mock[HttpGet with HttpPost with HttpPut]
  val mockSessionCache = mock[SessionCache]
  val sessionId = UUID.randomUUID.toString


  object TargetSubmissionConnector extends SubmissionConnector {
    override val sessionCache = mockSessionCache
    override val serviceUrl = "dummy"
    override val http = mockHttp

  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  "Calling checkLifetimeAllowanceExceeded" should {

    val validResponse = true
    val trueResponse = true
    val falseResponse = false

    when(mockHttp.GET[Option[Boolean]](Matchers.anyString())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(validResponse)))

    "return a valid response" in {

      val isKi: Boolean = true
      val previousInvestmentSchemesTotal: Int = 1000
      val proposedAmount: Int = 1000

      val result = TargetSubmissionConnector.checkLifetimeAllowanceExceeded(isKi, previousInvestmentSchemesTotal, proposedAmount)
      await(result) shouldBe Some(validResponse)
    }
  }
}
