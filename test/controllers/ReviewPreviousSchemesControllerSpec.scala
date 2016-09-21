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

import auth.MockAuthConnector
import builders.SessionBuilder
import common.{KeystoreKeys, Constants}
import config.FrontendAppConfig
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
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val model = PreviousSchemeModel(
    Constants.PageInvestmentSchemeEisValue, 2356, None, None, Some(4), Some(12), Some(2009), Some(1))
  val model2 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeSeisValue, 2356, Some(666), None, Some(4), Some(12), Some(2010), Some(3))
  val model3 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 2356, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))

  val emptyVectorList = Vector[PreviousSchemeModel]()
  val previousSchemeVectorList = Vector(model, model2, model3)
  val previousSchemeVectorListDeleted = Vector(model2, model3)
  val backLink = "/investment-tax-relief/previous-investment"

  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(previousSchemeVectorList)))
  val cacheMapEmpty: CacheMap = CacheMap("", Map("" -> Json.toJson(emptyVectorList)))
  val cacheMapDeleted: CacheMap = CacheMap("", Map("" -> Json.toJson(previousSchemeVectorListDeleted)))
  val cacheMapBackLink: CacheMap = CacheMap("", Map("" -> Json.toJson(backLink)))

  val testId = 1

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

  def addWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = ReviewPreviousSchemesControllerTest.add.apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def changeWithSession(processingId: Option[Int] = None)(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = ReviewPreviousSchemesControllerTest.change(processingId.get).apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def removeWithSession(processingId: Option[Int] = None)(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = ReviewPreviousSchemesControllerTest.remove(processingId.get).apply(SessionBuilder.buildRequestWithSession(sessionId))
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
              .thenReturn(Future.successful(Option(previousSchemeVectorList)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "return a 200 OK when a empty vector is returned from keystore" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyVectorList)))
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
        .thenReturn(Future.successful(Option(previousSchemeVectorList)))
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

  "Sending a Post request to PreviousSchemeController delete method" should {
    "redirect to 'Review previous scheme' and delete element from vector when an element with the given processing id is found" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]]
        (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeVectorList)))
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMapDeleted)
      removeWithSession(Some(1))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/review-previous-schemes")
        }
      )
    }

    "redirect to 'Review previous scheme' and return not delete from vector when an element with the given processing id is not found" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]]
        (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeVectorList)))
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      removeWithSession(Some(10))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/review-previous-schemes")
        }
      )
    }

    "redirect to 'Review previous scheme' when the vector is empty" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]]
        (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMapEmpty)
      removeWithSession(Some(1))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/review-previous-schemes")
        }
      )
    }
  }

  "Sending a GET request to ReviewPreviousSchemeController add method" should {
    "redirect to the previous investment scheme page" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMapBackLink)
      addWithSession(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/previous-investment")
        }
      )
    }
  }

  "Sending a GET request to ReviewPreviousSchemeController change method" should {
    "redirect to the previous investment scheme page" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMapBackLink)
      changeWithSession(Some(testId))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/previous-investment?id=" + testId)
        }
      )
    }
  }


}
