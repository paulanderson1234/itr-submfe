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

class CommercialSaleControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object CommercialSaleControllerTest extends CommercialSaleController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val keyStoreSavedCommercialSale = CommercialSaleModel(Constants.StandardRadioButtonYesValue, Some(15),Some(3),Some(1996))

  val model = CommercialSaleModel(Constants.StandardRadioButtonYesValue, Some(23),Some(11),Some(1993))
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))

  val savedKIDateconditionMet = KiProcessingModel(None, Some(true), Some(false), Some(false), Some(false))
  val savedKIDateconditionNotMet = KiProcessingModel(Some(false),Some(false), Some(false), Some(false), Some(false))
  val savedKIDateConditionEmpty = KiProcessingModel(Some(true), None, Some(false), Some(false), Some(false))
  val emptyKIModel = KiProcessingModel(None, None, None, None, None, None)

  val keyStoreSavedDateOfIncorporation = DateOfIncorporationModel(Some(21),Some(2),Some(2015))


  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = CommercialSaleControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = CommercialSaleControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "CommercialSaleController" should {
    "use the correct keystore connector" in {
      CommercialSaleController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to CommercialSaleController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid Yes form submission to the CommercialSaleController" should {
    "redirect to the KI page if the KI date condition is met" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> Constants.StandardRadioButtonYesValue,
        "commercialSaleDay" -> "23",
        "commercialSaleMonth" -> "11",
        "commercialSaleYear" -> "1993")
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(savedKIDateconditionMet)))
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/is-knowledge-intensive")
        }
      )
    }
  }

  "Sending a valid Yes form submission to the CommercialSaleController" should {
    "redirect to the subsidiaries page if the KI date condition is not met" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> Constants.StandardRadioButtonYesValue,
        "commercialSaleDay" -> "23",
        "commercialSaleMonth" -> "11",
        "commercialSaleYear" -> "1993")
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(savedKIDateconditionNotMet)))
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }

  "Sending a valid No form submission with a empty KI Model to the CommercialSaleController" should {
    "redirect to the date of incorporation page" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> Constants.StandardRadioButtonYesValue,
        "commercialSaleDay" -> "23",
        "commercialSaleMonth" -> "11",
        "commercialSaleYear" -> "1993")
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending a valid No form submission to the CommercialSaleController" should {
    "redirect to the KI page if the KI date condition is met" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> Constants.StandardRadioButtonNoValue,
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(savedKIDateconditionMet)))
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/is-knowledge-intensive")
        }
      )
    }
  }

  "Sending a valid No form submission with a Ki Model which has missing data to the CommercialSaleController" should {
    "redirect to the date of incorporation page" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> Constants.StandardRadioButtonNoValue,
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(savedKIDateConditionEmpty)))
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending a valid No form submission to the CommercialSaleController" should {
    "redirect to the subsidiaries page if the KI date condition is not met" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> Constants.StandardRadioButtonNoValue,
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(savedKIDateconditionNotMet)))
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }

}
