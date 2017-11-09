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
import common.Constants
import config.WSHttp
import controllers.helpers.FakeRequestHelper
import fixtures.SubmissionFixture
import play.api.test.Helpers._
import models.internal.CSApplicationModel
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

class ComplianceStatementConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneAppPerSuite with SubmissionFixture {

  object TestComplianceStatementConnector extends ComplianceStatementConnector with FakeRequestHelper{
    override val serviceUrl: String = MockConfig.internalCSSubmissionUrl
    override val http = mock[WSHttp]
  }

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1013")))
  implicit val user: TAVCUser = TAVCUser(ggUser.allowedAuthContext, internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5")

  val cSApplicationModel = CSApplicationModel(true, Some(Constants.schemeTypeEis))

  "ComplianceStatementConnector" should {
    "use correct http client" in {
      ComplianceStatementConnector.http shouldBe WSHttp
    }
  }

  "Calling getComplianceStatementApplication" when {
    "expecting a successful response" should {
      lazy val result = TestComplianceStatementConnector.getComplianceStatementApplication()
      "return a valid boolean response" in {
        when(TestComplianceStatementConnector.http.GET[CSApplicationModel](
          Matchers.eq(s"${TestComplianceStatementConnector.serviceUrl}/internal/cs-application-in-progress"))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(cSApplicationModel))
        await(result) match {
          case response => response shouldBe cSApplicationModel
          case _ => fail("No response was received, when one was expected")
        }
      }
    }
  }


  "Calling deleteCSApplication" when {
    "expecting a successful response" should {
      lazy val result = TestComplianceStatementConnector.deleteComplianceStatementApplication()
      "return a valid http response" in {
        when(TestComplianceStatementConnector.http.POSTEmpty[HttpResponse](
          Matchers.eq(s"${TestComplianceStatementConnector.serviceUrl}/internal/delete-cs-application"))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(NO_CONTENT)))
        await(result) match {
          case response => response.status shouldBe NO_CONTENT
          case _ => fail("No response was received, when one was expected")
        }
      }
    }
  }
}
