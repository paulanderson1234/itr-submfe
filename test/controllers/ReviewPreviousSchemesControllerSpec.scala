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
import controllers.Helpers.ControllerHelpers
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class ReviewPreviousSchemesControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]
  val mockControllerHelper = mock[ControllerHelpers]

  object ReviewPreviousSchemesControllerTest extends ReviewPreviousSchemesController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val previousSchemesList = Vector(PreviousSchemeModel("Enterprise Investment Scheme",23000,None,None,Some(23),Some(11),Some(1993),Some(1)),
    PreviousSchemeModel("Enterprise Investment Scheme",1101,None,None,Some(9),Some(11),Some(2015),Some(2)))
  val emptyPreviousSchemesList  = Vector[PreviousSchemeModel]()

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = ReviewPreviousSchemesControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = ReviewPreviousSchemesControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }


  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "ReviewPreviousSchemesController" should {
    "use the correct keystore connector" in {
      ReviewPreviousSchemesController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to ReviewPreviousSchemesController" should {
    "return a 200 OK when a populated vector is returned from keystore" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.any())(Matchers.any(), Matchers.any()))
              .thenReturn(Future.successful(Option(previousSchemesList)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "return a 200 OK when a empty vector is returned from keystore" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyPreviousSchemesList)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "return a 200 OK when nothing is returned from keystore (recover bloack exectued which creates empty vector)" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Posting to the continue button on the ReviewPreviousSchemesController" should {
    "redirect to 'Proposed Investment' page if table is not empty" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemesList)))
      val request = FakeRequest().withFormUrlEncodedBody()

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/proposed-investment")
        }
      )
    }

    "redirect to itself if table is empty" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      val request = FakeRequest().withFormUrlEncodedBody()

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/review-previous-schemes")
        }
      )
    }
  }

}
