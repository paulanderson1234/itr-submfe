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

class SubsidiariesSpendingInvestmentControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object SubsidiariesSpendingInvestmentControllerTest extends SubsidiariesSpendingInvestmentController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val modelYes = SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonYesValue)
  val modelNo = SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonNoValue)
  val emptyModel = SubsidiariesSpendingInvestmentModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(modelYes)))
  val keyStoreSavedSubsidiariesSpendingInvestment = SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonYesValue)

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = SubsidiariesSpendingInvestmentControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = SubsidiariesSpendingInvestmentControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "SubsidiariesSpendingInvestmentController" should {
    "use the correct keystore connector" in {
      SubsidiariesSpendingInvestmentController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to SubsidiariesSpendingInvestmentController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesSpendingInvestment)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.WhatWillUseForController.show().toString())))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.WhatWillUseForController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 300 when no back link is fetched using keystore" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the SubsidiariesSpendingInvestmentController" should {
    "redirect to the subsidiaries-ninety-percent-owned page" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "subSpendingInvestment" -> Constants.StandardRadioButtonYesValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries-ninety-percent-owned")
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the SubsidiariesSpendingInvestmentController" should {
    "redirect to the how-plan-to-use-investment page" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "subSpendingInvestment" -> Constants.StandardRadioButtonNoValue)
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/how-plan-to-use-investment")
        }
      )
    }
  }

  "Sending a invalid form submit to the SubsidiariesSpendingInvestmentController with no backlink" should {
    "redirect to the subsidiaries-ninety-percent-owned page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      val request = FakeRequest().withFormUrlEncodedBody(
        "subSpendingInvestment" -> "")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the SubsidiariesSpendingInvestmentController" should {
    "redirect to itself with errors" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.WhatWillUseForController.show().toString())))
      val request = FakeRequest().withFormUrlEncodedBody(
        "subSpendingInvestment" -> "")
      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
