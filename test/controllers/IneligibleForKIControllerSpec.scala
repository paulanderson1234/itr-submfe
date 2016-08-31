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
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class IneligibleForKIControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication{


  val mockKeyStoreConnector = mock[KeystoreConnector]

  object IneligibleForKIControllerTest extends IneligibleForKIController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
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

  "Sending a GET request to IneligibleForKIController" should {
    "return a 200" in {
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Posting to the IneligibleForKIController" should {
    "redirect to 'Subsidiaries' page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.PercentageStaffWithMastersController.show().toString())))
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
