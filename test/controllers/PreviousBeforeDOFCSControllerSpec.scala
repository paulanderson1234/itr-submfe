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
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class PreviousBeforeDOFCSControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object PreviousBeforeDOFCSControllerTest extends PreviousBeforeDOFCSController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val modelYes = PreviousBeforeDOFCSModel(Constants.StandardRadioButtonYesValue)
  val modelNo = PreviousBeforeDOFCSModel(Constants.StandardRadioButtonNoValue)
  val emptyModel = PreviousBeforeDOFCSModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(modelYes)))
  val keyStoreSavedPreviousBeforeDOFCS = PreviousBeforeDOFCSModel(Constants.StandardRadioButtonYesValue)
  val keyStoreSavedSubsidiariesYes = SubsidiariesModel(Constants.StandardRadioButtonYesValue)
  val keyStoreSavedSubsidiariesNo = SubsidiariesModel(Constants.StandardRadioButtonNoValue)

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = PreviousBeforeDOFCSControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = PreviousBeforeDOFCSControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "PreviousBeforeDOFCSController" should {
    "use the correct keystore connector" in {
      PreviousBeforeDOFCSController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to PreviousBeforeDOFCSController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedPreviousBeforeDOFCS)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid 'No' form submit to the PreviousBeforeDOFCSController" should {
    "redirect to new geographical market" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "previousBeforeDOFCS" -> Constants.StandardRadioButtonNoValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/new-geographical-market")
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the PreviousBeforeDOFCSController with 'No' to Subsidiaries Model" should {
    "redirect to the how-plan-to-use-investment page" in {
     when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "previousBeforeDOFCS" -> Constants.StandardRadioButtonYesValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/how-plan-to-use-investment")
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the PreviousBeforeDOFCSController with 'Yes' to Subsidiaries Model" should {
    "redirect to the subsidiaries-spending-investment page" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "previousBeforeDOFCS" -> Constants.StandardRadioButtonYesValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries-spending-investment")
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the PreviousBeforeDOFCSController with 'Yes' to Subsidiaries Model" should {
    "redirect to new geographical market" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "previousBeforeDOFCS" -> Constants.StandardRadioButtonNoValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/new-geographical-market")
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the PreviousBeforeDOFCSController with 'No' to Subsidiaries Model" should {
    "redirect to new geographical market" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "previousBeforeDOFCS" -> Constants.StandardRadioButtonNoValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/new-geographical-market")
        }
      )
    }
  }

  "Sending a valid form submit to the PreviousBeforeDOFCSController without a Subsidiaries Model" should {
    "redirect to Subsidiaries page" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      val request = FakeRequest().withFormUrlEncodedBody(
        "previousBeforeDOFCS" -> Constants.StandardRadioButtonYesValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }


  "Sending an invalid form submission with validation errors to the PreviousBeforeDOFCSController" should {
    "redirect to itself" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "previousBeforeDOFCS" -> "")
      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}