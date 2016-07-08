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

class OperatingCostsControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object OperatingCostsControllerTest extends OperatingCostsController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val operatingCostsAsJson =
    """{"operatingCosts1stYear" : 750000, "operatingCosts2ndYear" : 800000, "operatingCosts3rdYear" : 934000,
      | "rAndDCosts1stYear" : 231000, "rAndDCosts2ndYear" : 340000, "rAndDCosts3rdYear" : 344000}""".stripMargin

  val model = OperatingCostsModel("200000", "225000", "270000", "177000", "188000", "19000")
  val emptyModel = OperatingCostsModel("", "", "", "", "", "")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val keyStoreSavedOperatingCosts = OperatingCostsModel("200000", "225000", "270000", "177000", "188000", "19000")

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = OperatingCostsControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = OperatingCostsControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "OperatingCostsController" should {
    "use the correct keystore connector" in {
      OperatingCostsController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to OperatingCostsController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedOperatingCosts)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

  }

  "Sending a valid form submit to the OperatingCostsController" should {
    "redirect to the Operting Costs  page" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "750000",
        "operatingCosts2ndYear" -> "750000",
        "operatingCosts3rdYear" -> "934000",
        "rAndDCosts1stYear" -> "231000",
        "rAndDCosts2ndYear" -> "340000",
        "rAndDCosts3rdYear" -> "344000")

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/operating-costs")
        }
      )
    }
  }

  "Sending an empty invalid form submission with validation errors to the CommercialSaleController" should {
    "redirect to itself" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "",
        "operatingCosts2ndYear" -> "",
        "operatingCosts3rdYear" -> "",
        "rAndDCosts1stYear" -> "",
        "rAndDCosts2ndYear" -> "",
        "rAndDCosts3rdYear" -> "")

      submitWithSession(request)(
        result => {
          status(result) shouldBe 400
          //redirectLocation(result) shouldBe Some(routes.OperatingCostsController.show().toString())
        }
      )
    }
  }


  "Sending an invalid form with missing data submission with validation errors to the OperatingCostsController" should {
    "redirect to itself" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "230000",
        "operatingCosts2ndYear" -> "189250",
        "operatingCosts3rdYear" -> "300000",
        "rAndDCosts1stYear" -> "",
        "rAndDCosts2ndYear" -> "",
        "rAndDCosts3rdYear" -> "")

      submitWithSession(request)(
        result => {
          status(result) shouldBe 400
          //redirectLocation(result) shouldBe Some(routes.OperatingCostsController.show().toString())
        }
      )
    }
  }

}
