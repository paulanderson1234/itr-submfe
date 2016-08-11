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

class InvestmentGrowControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object InvestmentGrowControllerTest extends InvestmentGrowController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val model = InvestmentGrowModel("some text")
  val emptyModel = InvestmentGrowModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val keyStoreSavedInvestmentGrow = InvestmentGrowModel("text some other")

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = InvestmentGrowControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = InvestmentGrowControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "InvestmentGrowController" should {
    "use the correct keystore connector" in {
      InvestmentGrowController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to InvestmentGrowController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedInvestmentGrow)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(routes.SubsidiariesNinetyOwnedController.show().toString())))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.SubsidiariesNinetyOwnedController.show().toString())))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 300 when no back link is fetched using keystore" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit to the InvestmentGrowController" should {
    "redirect to Contact Details Controller" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "investmentGrowDesc" -> "some text so it's valid")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/contact-details")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the NewGeographicalMarketController with no backlink" should {
    "redirect to WhatWillUseFor page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      val request = FakeRequest().withFormUrlEncodedBody(
        "investmentGrowDesc" -> "")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the InvestmentGrowController" should {
    "redirect to itself with errors" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.SubsidiariesNinetyOwnedController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(keyStoreSavedInvestmentGrow)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "investmentGrowDesc" -> "")
      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
