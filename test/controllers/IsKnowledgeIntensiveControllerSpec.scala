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
import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
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

class IsKnowledgeIntensiveControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object IsKnowledgeIntensiveControllerTest extends IsKnowledgeIntensiveController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val modelYes = IsKnowledgeIntensiveModel(Constants.StandardRadioButtonYesValue)
  val modelNo = IsKnowledgeIntensiveModel(Constants.StandardRadioButtonNoValue)
  val emptyModel = IsKnowledgeIntensiveModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(modelYes)))
  val keyStoreSavedIsKnowledgeIntensive = IsKnowledgeIntensiveModel(Constants.StandardRadioButtonYesValue)

  val updatedKIModel = KiProcessingModel(Some(true),Some(false), Some(false), Some(false), None, Some(false))
  val falseKIModel = KiProcessingModel(Some(false),Some(false), Some(false), Some(false), None, Some(false))
  val missingDateKIModel = KiProcessingModel(Some(true),None, Some(false), Some(false), None, Some(false))

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = IsKnowledgeIntensiveControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = IsKnowledgeIntensiveControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "IsKnowledgeIntensiveController" should {
    "use the correct keystore connector" in {
      IsKnowledgeIntensiveController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to IsKnowledgeIntensiveController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensive)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid 'Yes' form submit to the IsKnowledgeIntensiveController" should {
    "redirect to the operating costs page" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(updatedKIModel)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "isKnowledgeIntensive" -> Constants.StandardRadioButtonYesValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/operating-costs")
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit with missing data in the KI Model to the IsKnowledgeIntensiveController" should {
    "redirect to the date of incorporation page" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(missingDateKIModel)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "isKnowledgeIntensive" -> Constants.StandardRadioButtonYesValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending a valid 'No' form submit with a false KI Model to the IsKnowledgeIntensiveController" should {
    "redirect to the subsidiaries" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "isKnowledgeIntensive" -> Constants.StandardRadioButtonNoValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }

  "Sending a valid 'No' form submit without a KI Model to the IsKnowledgeIntensiveController" should {
    "redirect to the date of incorporation" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      val request = FakeRequest().withFormUrlEncodedBody(
        "isKnowledgeIntensive" -> Constants.StandardRadioButtonNoValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the IsKnowledgeIntensiveController" should {
    "redirect to the subsidiaries" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(updatedKIModel)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "isKnowledgeIntensive" -> Constants.StandardRadioButtonNoValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }
  
  "Sending an invalid form submission with validation errors to the IsKnowledgeIntensiveController" should {
    "redirect to itself" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "isKnowledgeIntensive" -> "")
      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
