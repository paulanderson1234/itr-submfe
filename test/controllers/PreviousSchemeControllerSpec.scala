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

class PreviousSchemeControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object PreviousSchemeControllerTest extends PreviousSchemeController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val model = PreviousSchemeModel(
    Constants.PageInvestmentSchemeEisValue, 2356, None, None, Some(4), Some(12), Some(2009), Some(1))
  val model2 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeSeisValue, 2356, Some(666), None, Some(4), Some(12), Some(2010), Some(3))
  val model3 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 2356, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model3Updated = PreviousSchemeModel(
    Constants.PageInvestmentSchemeSeisValue, 6666, Some(777), None, Some(7), Some(3), Some(2015), Some(5))

  val emptyVectorList = Vector[PreviousSchemeModel]()

  val previousSchemeVectorList = Vector(model, model2, model3)

  val previousSchemeVectorListUpdated = Vector(model, model2, model3Updated)

  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(previousSchemeVectorList)))
  val cacheMapUpdated: CacheMap = CacheMap("", Map("" -> Json.toJson(previousSchemeVectorListUpdated)))


  def showWithSession(id: Option[Int] = None)(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = PreviousSchemeControllerTest.show(id).apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = PreviousSchemeControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "PreviousSchemeController" should {
    "use the correct keystore connector" in {
      PreviousSchemeController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to PreviousSchemeController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousSchemeModel]
        (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      showWithSession(None)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when None is fetched using keystore" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousSchemeModel]
        (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      showWithSession(Some(1))(
        result => status(result) shouldBe OK
      )
    }
  }

  "provide an empty model and return a 200 when an empty Vector List is fetched using keystore" in {
    when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]]
      (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(emptyVectorList)))
    when(mockKeyStoreConnector.fetchAndGetFormData[String]
      (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
    showWithSession(Some(1))(
      result => status(result) shouldBe OK
    )
  }

  "provide an populated model and return a 200 when model with matching Id is fetched using keystore" in {
    when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]]
      (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(previousSchemeVectorList)))
    when(mockKeyStoreConnector.fetchAndGetFormData[String]
      (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
    showWithSession(Some(3))(
      result => status(result) shouldBe OK
    )
  }

  "Sending a valid new form submit to the PreviousSchemeController" should {
    "create a new item and redirect to the review previous investments page" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(),
        Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]]
        (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeVectorList)))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      val request = FakeRequest().withFormUrlEncodedBody(
        "schemeTypeDesc" -> Constants.PageInvestmentSchemeAnotherValue,
        "investmentAmount" -> "12345",
        "investmentSpent" -> "",
        "otherSchemeName" -> "money making scheme",
        "investmentDay" -> "3",
        "investmentMonth" -> "8",
        "investmentYear" -> "1988",
        "processingId" -> ""

      )
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/review-previous-schemes")
        }
      )
    }
  }

  "Sending a valid updated form submit to the PreviousSchemeController" should {
    "update the item and redirect to the review previous investments page" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(),
        Matchers.any())).thenReturn(cacheMapUpdated)
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]]
        (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeVectorList)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      val request = FakeRequest().withFormUrlEncodedBody(
        "schemeTypeDesc" -> Constants.PageInvestmentSchemeSeisValue,
        "investmentAmount" -> "666",
        "investmentSpent" -> "777",
        "otherSchemeName" -> "",
        "investmentDay" -> "7",
        "investmentMonth" -> "3",
        "investmentYear" -> "2015",
        "processingId" -> "5"

      )
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/review-previous-schemes")
        }
      )
    }
  }

  "Sending a new (processingId ==0) invalid (no amount) form submit  to the PreviousSchemeController" should {
    "not create the item and redirect to itself with errors as a bad request" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(),
        Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]]
        (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeVectorList)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      val request = FakeRequest().withFormUrlEncodedBody(
        "schemeTypeDesc" -> Constants.PageInvestmentSchemeAnotherValue,
        "investmentAmount" -> "",
        "investmentSpent" -> "",
        "otherSchemeName" -> "money making scheme",
        "investmentDay" -> "3",
        "investmentMonth" -> "8",
        "investmentYear" -> "1988",
        "processingId" -> ""
      )
      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a invalid (no amount) updated form submit to the PreviousSchemeController" should {
    "not update the item and redirect to itself with errors as a bad request" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(cacheMapUpdated)
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]]
        (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeVectorList)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      val request = FakeRequest().withFormUrlEncodedBody(
        "schemeTypeDesc" -> Constants.PageInvestmentSchemeVctValue,
        "investmentAmount" -> "",
        "investmentSpent" -> "",
        "otherSchemeName" -> "",
        "investmentDay" -> "7",
        "investmentMonth" -> "3",
        "investmentYear" -> "2015",
        "processingId" -> "5"

      )
      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
