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

class WhatWillUseForControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object WhatWillUseForControllerTest extends WhatWillUseForController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }
  val keyStoreSavedIsKnowledgeIntensiveYes = IsKnowledgeIntensiveModel("Yes")
  val keyStoreSavedIsKnowledgeIntensiveNo = IsKnowledgeIntensiveModel("No")
  val keyStoreSavedIsKnowledgeIntensiveEmpty = IsKnowledgeIntensiveModel("")
  val keyStoreSavedHadPreviousRFIYes = HadPreviousRFIModel("Yes")
  val keyStoreSavedHadPreviousRFINo = HadPreviousRFIModel("No")
  val keyStoreSavedHadPreviousRFIEmpty = HadPreviousRFIModel("")
  val keyStoreSavedSubsidiariesYes = SubsidiariesModel("Yes")
  val keyStoreSavedSubsidiariesNo = SubsidiariesModel("No")
  val keyStoreSavedSubsidiariesEmpty = SubsidiariesModel("")
  val keyStoreSavedCommercialSaleOver10Years = CommercialSaleModel("Yes", Some(15),Some(3),Some(1996))
  val keyStoreSavedCommercialSaleOver7Years = CommercialSaleModel("Yes", Some(15),Some(3),Some(2008))
  val keyStoreSavedCommercialSaleOver3Years = CommercialSaleModel("Yes", Some(15),Some(3),Some(2013))
  val keyStoreSavedCommercialSaleNo = CommercialSaleModel("No", None , None , None)
  val keyStoreSavedCommercialSaleEmpty = CommercialSaleModel("", None , None , None)

  val modelBusiness = WhatWillUseForModel("Doing Business")
  val modelPrepare = WhatWillUseForModel("Getting ready to do business")
  val modelRAndD = WhatWillUseForModel("Research and Development")
  val emptyModel = WhatWillUseForModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(modelBusiness)))
  val keyStoreSavedWhatWillUseForBusiness = WhatWillUseForModel("Doing Business")
  val keyStoreSavedWhatWillUseForPrepare = WhatWillUseForModel("Getting ready to do business")
  val keyStoreSavedWhatWillUseForRAndD = WhatWillUseForModel("Research and Development")

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = WhatWillUseForControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = WhatWillUseForControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "WhatWillUseForController" should {
    "use the correct keystore connector" in {
      WhatWillUseForController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to WhatWillUseForController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedWhatWillUseForBusiness)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid 'Doing Business' form submit to the WhatWillUseForController" should {
    "redirect to itself for the moment" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Doing business")
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleOver3Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
        }
      )
    }
  }

  "Sending a valid 'Research and Development' form submit to the WhatWillUseForController" should {
    "redirect to itself for the moment" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Doing business")
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleOver3Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
        }
      )
    }
  }

  "Sending a valid 'Getting ready to do business' form submit to the WhatWillUseForController" should {
    "redirect itself for the moment" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleOver3Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
        }
      )
    }
  }

  "Sending a valid form submit to the WhatWillUseForController with PreviousRFI = true and when a Commercial sale has been made" should {
    "redirect itself for the moment" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleOver3Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/used-investment-reason-before")
        }
      )
    }
  }

  "Sending a valid form submit to the WhatWillUseForController with PreviousRFI = false, KI = false and Commercial sale over than 7 years" should {
    "redirect itself for the moment" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleOver7Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveNo)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/new-geographical-market")
        }
      )
    }
  }

  "Sending a valid form submit to the WhatWillUseForController with PreviousRFI = false, KI = true, sub = true and Commercial sale over than 7 years" should {
    "redirect itself for the moment" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleOver7Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit to the WhatWillUseForController with PreviousRFI = false, KI = false and Commercial sale over 3 years" should {
    "redirect itself for the moment" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleOver3Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveNo)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit to the WhatWillUseForController with PreviousRFI = false, KI = true, sub = false and Commercial sale over than 10 years" should {
    "redirect itself for the moment" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleOver10Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/new-geographical-market")
        }
      )
    }
  }

  "Sending a valid form submit to the WhatWillUseForController with PreviousRFI = false, KI = true, sub = false and Commercial sale = false" should {
    "redirect itself for the moment" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit to the WhatWillUseForController with PreviousRFI = true, KI = true, sub = false and Commercial sale = none" should {
    "redirect itself for the moment" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Future.successful(None)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit to the WhatWillUseForController with PreviousRFI = false, KI = none, sub = none and Commercial sale = false" should {
    "redirect itself for the moment" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleOver3Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Future.successful(None)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveEmpty)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit to the WhatWillUseForController with PreviousRFI = false, KI = none, sub = true and Commercial sale = true" should {
    "redirect itself for the moment" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleOver3Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Future.successful(None)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit to the WhatWillUseForController with PreviousRFI = none, KI = true, sub = false and Commercial sale = true" should {
    "redirect itself for the moment" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Future.successful(None)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleOver3Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the WhatWillUseForController" should {
    "redirect to itself" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "")
      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
