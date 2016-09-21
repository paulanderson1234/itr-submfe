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

import auth.MockAuthConnector
import builders.SessionBuilder
import common.KeystoreKeys
import config.FrontendAppConfig
import connectors.{KeystoreConnector, SubmissionConnector}
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
  val mockSubmissionConnector = mock[SubmissionConnector]

  object OperatingCostsControllerTest extends OperatingCostsController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
    val submissionConnector: SubmissionConnector = mockSubmissionConnector
  }

  val operatingCostsAsJson =
    """{"operatingCosts1stYear" : 750000, "operatingCosts2ndYear" : 800000, "operatingCosts3rdYear" : 934000,
      | "rAndDCosts1stYear" : 231000, "rAndDCosts2ndYear" : 340000, "rAndDCosts3rdYear" : 344000}""".stripMargin

  val model = OperatingCostsModel("200000", "225000", "270000", "177000", "188000", "19000")
  val emptyModel = OperatingCostsModel("", "", "", "", "", "")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val keyStoreSaved0PercOperatingCCosts = OperatingCostsModel("4100200", "3600050", "4252500", "0", "0", "0")
  val keyStoreSaved10PercBoundaryOC = OperatingCostsModel("4100200", "3600050", "4252500", "410020", "360005", "425250")
  val operatingCosts10PercBoundaryOC = OperatingCostsModel("1000", "1000", "1000", "100", "100", "100")
  val keyStoreSaved15PercBoundaryOC = OperatingCostsModel("755500", "900300", "523450", "37775", "135045", "0")

  val trueKIModel = KiProcessingModel(Some(true), Some(true), Some(true), Some(true), None, Some(true))
  val dateConditionMetKIModel = KiProcessingModel(Some(true),Some(true), None, None, None, None)
  val falseKIModel = KiProcessingModel(Some(false), Some(false), Some(false), Some(false), None, Some(false))
  val emptyKIModel = KiProcessingModel(None, None, None, None, None, None)
  val missingKIModel = KiProcessingModel(None,Some(true),None, None, None, None)

  val operatingCosts1 = 1000
  val rAndDCosts1 = 100
  val rAndDCosts2 = 0

  val operatingCostsTrueKIVectorList = Vector(keyStoreSaved15PercBoundaryOC)
  val operatingCostsFalseKIVectorList = Vector(operatingCosts1, operatingCosts1, operatingCosts1,rAndDCosts2,rAndDCosts2,rAndDCosts2)

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
        .thenReturn(Future.successful(Option(keyStoreSaved10PercBoundaryOC)))
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
    "redirect to the Percentage Of Staff With Masters page (for now)" in {
      when(mockSubmissionConnector.validateKiCostConditions(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(true)))
      when(mockKeyStoreConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "1000",
        "operatingCosts2ndYear" -> "1000",
        "operatingCosts3rdYear" -> "1000",
        "rAndDCosts1stYear" -> "100",
        "rAndDCosts2ndYear" -> "100",
        "rAndDCosts3rdYear" -> "100")

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/percentage-of-staff-with-masters")
        }
      )
    }
  }

  "Sending a valid form submit to the OperatingCostsController but not KI" should {
    "redirect to the Ineligible For KI page" in {
      when(mockSubmissionConnector.validateKiCostConditions(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "100",
        "operatingCosts2ndYear" -> "100",
        "operatingCosts3rdYear" -> "100",
        "rAndDCosts1stYear" -> "1",
        "rAndDCosts2ndYear" -> "1",
        "rAndDCosts3rdYear" -> "1")

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/ineligible-for-knowledge-intensive")
        }
      )
    }
  }

  "Sending a invalid form submit to the OperatingCostsController" should {
    "return a bad request" in {

      when(mockKeyStoreConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "0",
        "operatingCosts2ndYear" -> "0",
        "operatingCosts3rdYear" -> "0",
        "rAndDCosts1stYear" -> "0",
        "rAndDCosts2ndYear" -> "0",
        "rAndDCosts3rdYear" -> "0")

      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending an empty KI Model to the OperatingCostsController" should {
    "redirect to DateOfIncorporation page" in {
      when(mockSubmissionConnector.validateKiCostConditions(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyKIModel)))

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "100",
        "operatingCosts2ndYear" -> "100",
        "operatingCosts3rdYear" -> "100",
        "rAndDCosts1stYear" -> "10",
        "rAndDCosts2ndYear" -> "10",
        "rAndDCosts3rdYear" -> "10")

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending a KI Model set as None to the OperatingCostsController" should {
    "redirect to DateOfIncorporation page" in {
      when(mockSubmissionConnector.validateKiCostConditions(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "100",
        "operatingCosts2ndYear" -> "100",
        "operatingCosts3rdYear" -> "100",
        "rAndDCosts1stYear" -> "10",
        "rAndDCosts2ndYear" -> "10",
        "rAndDCosts3rdYear" -> "10")

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending an KI Model with missing data to the OperatingCostsController" should {
    "redirect to DateOfIncorporation page" in {
      when(mockSubmissionConnector.validateKiCostConditions(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(missingKIModel)))

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "100",
        "operatingCosts2ndYear" -> "100",
        "operatingCosts3rdYear" -> "100",
        "rAndDCosts1stYear" -> "10",
        "rAndDCosts2ndYear" -> "10",
        "rAndDCosts3rdYear" -> "10")

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending an non KI Model to the OperatingCostsController" should {
    "redirect to IsKI page" in {
      when(mockSubmissionConnector.validateKiCostConditions(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "100",
        "operatingCosts2ndYear" -> "100",
        "operatingCosts3rdYear" -> "100",
        "rAndDCosts1stYear" -> "0",
        "rAndDCosts2ndYear" -> "0",
        "rAndDCosts3rdYear" -> "0")

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/is-knowledge-intensive")
        }
      )
    }
  }

  "Sending an empty invalid form submission with validation errors to the CommercialSaleController" should {
    "return a bad request" in {

      when(mockKeyStoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSaved10PercBoundaryOC)))

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "",
        "operatingCosts2ndYear" -> "",
        "operatingCosts3rdYear" -> "",
        "rAndDCosts1stYear" -> "",
        "rAndDCosts2ndYear" -> "",
        "rAndDCosts3rdYear" -> "")

      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
          //redirectLocation(result) shouldBe Some(routes.OperatingCostsController.show().toString())
        }
      )
    }
  }


  "Sending an invalid form with missing data submission with validation errors to the OperatingCostsController" should {
    "return a bad request" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "230000",
        "operatingCosts2ndYear" -> "189250",
        "operatingCosts3rdYear" -> "300000",
        "rAndDCosts1stYear" -> "",
        "rAndDCosts2ndYear" -> "",
        "rAndDCosts3rdYear" -> "")

      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
          //redirectLocation(result) shouldBe Some(routes.OperatingCostsController.show().toString())
        }
      )
    }
  }

  "Sending an invalid form with invalid data submission with validation errors to the OperatingCostsController" should {
    "return a bad request" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "operatingCosts1stYear" -> "230000",
        "operatingCosts2ndYear" -> "189250",
        "operatingCosts3rdYear" -> "300000",
        "rAndDCosts1stYear" -> "aaaaa",
        "rAndDCosts2ndYear" -> "10000",
        "rAndDCosts3rdYear" -> "12000")

      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
          //redirectLocation(result) shouldBe Some(routes.OperatingCostsController.show().toString())
        }
      )
    }
  }

}
