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

class ProposedInvestmentControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object ProposedInvestmentControllerTest extends ProposedInvestmentController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val model = ProposedInvestmentModel(5000000)
  val emptyModel = ProposedInvestmentModel(0)
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val keyStoreSavedProposedInvestment = ProposedInvestmentModel(1234568)

  val trueKIModel = KiProcessingModel(Some(true), Some(true), Some(true), Some(true), None, Some(true))
  val falseKIModel = KiProcessingModel(Some(false), Some(false), Some(false), Some(false), None, Some(false))
  val emptyKIModel = KiProcessingModel(None, None, None, None, None, None)

  val EISSchemeModel = PreviousSchemeModel("Enterprise Investment Scheme", 30000, None, None, None, None, None, None)
  val SEISSchemeModel = PreviousSchemeModel("Seed Enterprise Investment Scheme", 30000000, None, None, None, None, None, None)
  val emptySchemeModel = PreviousSchemeModel("", 0, None, None, None, None, None, None)

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = ProposedInvestmentControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = ProposedInvestmentControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "ProposedInvestmentController" should {
    "use the correct keystore connector" in {
      ProposedInvestmentController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to ProposedInvestmentController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel]
        (Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedProposedInvestment)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)

      when(mockKeyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel]
        (Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to ProposedInvestmentController without a valid backlink from keystore" should {
    "redirect to the beginning of the flow" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/used-investment-scheme-before")
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController" should {
    "redirect to the  company's registered address page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))

      val request = FakeRequest().withFormUrlEncodedBody(
        "investmentAmount" -> "1234567")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ProposedInvestmentController" should {
    "redirect with a bad request" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      val request = FakeRequest().withFormUrlEncodedBody(
        "investmentAmount" -> "fff")
      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
