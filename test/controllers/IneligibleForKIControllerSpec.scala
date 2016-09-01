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

package controllers

import java.util.UUID

import builders.SessionBuilder
import common.KeystoreKeys
import connectors.KeystoreConnector
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite

import scala.concurrent.Future

class IneligibleForKIControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with WithFakeApplication{


  val mockKeyStoreConnector = mock[KeystoreConnector]

  object IneligibleForKIControllerTest extends IneligibleForKIController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = IneligibleForKIControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = IneligibleForKIControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  "IneligibleForKIController" should {
    "use the correct keystore connector" in {
      IneligibleForKIController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to IneligibleForKIController without a valid backlink from keystore" should {
    "redirect to the beginning of the flow" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkIneligibleForKI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/operating-costs")
        }
      )
    }
  }

  "Sending a GET request to IneligibleForKIController with a valid back link" should {
    "return a 200" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkIneligibleForKI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.OperatingCostsController.show().toString())))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Posting to the IneligibleForKIController" should {
    "redirect to 'Subsidiaries' page" in {

      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkIneligibleForKI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.OperatingCostsController.show().toString())))
      val request = FakeRequest().withFormUrlEncodedBody()

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }

}
