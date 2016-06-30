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
import connectors.KeystoreConnector
import controllers.HowToApplyController
import controllers.examples.{ContactDetailsController, routes}
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.{FakeApplication, FakeRequest}
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class HowToApplyControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication{

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = HowToApplyController.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = HowToApplyController.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()


  "Sending a GET request to HowToApplyController" should {
    "return a 200" in {
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

  }


  ""

  "Posting to the HowToApplyController" should {
    "redirect to 'What does your company need' page" in {

      val request = FakeRequest().withFormUrlEncodedBody()

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/how-to-apply")
        }
      )
    }
  }

}
