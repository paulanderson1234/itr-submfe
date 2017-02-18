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

import controllers.helpers.FakeRequestHelper
import fixtures.SubmissionFixture
import org.mockito.Matchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SubscriptionConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with SubmissionFixture {

  object TargetSubscriptionConnector extends SubscriptionConnector with FrontendController with FakeRequestHelper with ServicesConfig {
    override val serviceUrl = baseUrl("investment-tax-relief-subscription")
    override val http = mock[WSHttp]
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1234")))
  val validTavcReference = "XATAVC000123456"

  val successResponse = HttpResponse(Status.OK, responseJson = Some(Json.parse(
    """
      |{
      |    "processingDate": "2001-12-17T09:30:47Z",
      |    "subscriptionType": {
      |        "safeId": "XA0000000012345",
      |        "correspondenceDetails": {
      |            "contactName": {
      |                "name1": "first",
      |                "name2": "last"
      |            },
      |            "contactDetails": {
      |                "phoneNumber": "0000 10000",
      |                "mobileNumber": "0000 2000",
      |                "faxNumber": "0000 30000",
      |                "emailAddress": "test@test.com"
      |            },
      |            "contactAddress": {
      |                "addressLine1": "12 some street",
      |                "addressLine2": "some line 2",
      |                "addressLine3": "some line 3",
      |                "addressLine4": "some line 4",
      |                "countryCode": "GB",
      |                "postalCode": "AA1 1AA"
      |            }
      |        }
      |    }
      |}
    """.stripMargin
  )))

  val failedResponse = HttpResponse(Status.BAD_REQUEST, responseJson = Some(Json.parse(
    """
      |{
      |    "reason": {
      |        "type": "Bad Request",
      |        "description": "The request was invalid"
      |    }
      |}
    """.stripMargin
  )))

  def setupMockedResponse(data: HttpResponse): OngoingStubbing[Future[HttpResponse]] = {
    when(TargetSubscriptionConnector.http.GET[HttpResponse](
      Matchers.eq(s"${TargetSubscriptionConnector.serviceUrl}/investment-tax-relief-subscription/$validTavcReference/subscription"))
      (Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))
  }

  "Calling getSubscriptonDetails" when {

    "expecting a successful response" should {
      lazy val result = TargetSubscriptionConnector.getSubscriptionDetails(validTavcReference)

      "return a Status OK (200) response" in {
        setupMockedResponse(successResponse)
        await(result) match {
          case Some(response) => response.status shouldBe Status.OK
          case _ => fail("No response was received, when one was expected")
        }
      }

      "Have a successful Json Body response" in {
        setupMockedResponse(successResponse)
        await(result) match {
          case Some(response) => response.json shouldBe successResponse.json
          case _ => fail("No response was received, when one was expected")
        }
      }
    }

    "expecting a non-successful response" should {
      lazy val result = TargetSubscriptionConnector.getSubscriptionDetails(validTavcReference)

      "return a Status BAD_REQUEST (400) response" in {
        setupMockedResponse(failedResponse)
        await(result) match {
          case Some(response) => response.status shouldBe Status.BAD_REQUEST
          case _ => fail("No response was received, when one was expected")
        }
      }

      "Have a non-successful Json Body response" in {
        setupMockedResponse(failedResponse)
        await(result) match {
          case Some(response) => response.json shouldBe failedResponse.json
          case _ => fail("No response was received, when one was expected")
        }
      }
    }
  }
}
