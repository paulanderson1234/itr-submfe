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

class SubsidiariesControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object SubsidiariesControllerTest extends SubsidiariesController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val modelYes = SubsidiariesModel("Yes")
  val modelNo = SubsidiariesModel("No")
  val emptyModel = SubsidiariesModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(modelYes)))
  val keyStoreSavedSubsidiaries = SubsidiariesModel("Yes")
  val keyStoreSavedDateOfIncorporation = DateOfIncorporationModel(Some(2),Some(3),Some(2016))

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = SubsidiariesControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = SubsidiariesControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "SubsidiariesController" should {
    "use the correct keystore connector" in {
      SubsidiariesController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to SubsidiariesController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiaries)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid 'Yes' form submit to the SubsidiariesController" should {
    "redirect to itself (for now)" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.eq(KeystoreKeys.subsidiaries), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "subsidiaries" -> "Yes")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the SubsidiariesController" should {
    "redirect to itself (for now)" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.eq(KeystoreKeys.subsidiaries), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "subsidiaries" -> "No")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }

  "Sending an invalid form wsubmission with validation errors to the SubsidiariesController" should {
    "redirect to itself with errors" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "ownSubsidiaries" -> "")
      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}