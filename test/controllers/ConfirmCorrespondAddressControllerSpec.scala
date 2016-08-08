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
import common.Constants
import connectors.KeystoreConnector
import models.ConfirmCorrespondAddressModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class ConfirmCorrespondAddressControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]


  object ConfirmCorrespondAddressControllerTest extends ConfirmCorrespondAddressController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val model = ConfirmCorrespondAddressModel(Constants.StandardRadioButtonYesValue)
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val keyStoreSavedConfirmCorrespondAddress = ConfirmCorrespondAddressModel(Constants.StandardRadioButtonYesValue)

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = ConfirmCorrespondAddressControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = ConfirmCorrespondAddressControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "ConfirmCorrespondAddressController" should {
    "use the correct keystore connector" in {
      ConfirmCorrespondAddressController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to ConfirmCorrespondAddressController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedConfirmCorrespondAddress)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid form submission with Yes option to the ConfirmCorrespondAddressController" should {
    "redirect to own page" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "contactAddressUse" -> Constants.StandardRadioButtonYesValue
      )
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/confirm-correspondence-address")
        }
      )
    }
  }

  "Sending a valid form submission with No option to the ConfirmCorrespondAddressController" should {
    "redirect to own page" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "contactAddressUse" -> Constants.StandardRadioButtonNoValue
      )
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/confirm-correspondence-address")
        }
      )
    }
  }

  "Sending an empty invalid form submission with validation errors to the ConfirmCorrespondAddressController" should {
    "redirect to itself" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "contactAddressUse" -> ""
       )

      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
