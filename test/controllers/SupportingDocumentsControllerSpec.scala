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
import org.scalatest.mock.MockitoSugar
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import scala.concurrent.Future

class SupportingDocumentsControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = SupportingDocumentsController.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = SupportingDocumentsController.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  "Sending a GET request to SupportingDocumentsController" should {
    "return a 200 OK" in {
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Posting to the SupportingDocumentsController" should {
    "redirect to itself" in {

      val request = FakeRequest().withFormUrlEncodedBody()

      submitWithSession(request)(result => {
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("/investment-tax-relief/supporting-documents")
      }
      )
    }
  }
}
