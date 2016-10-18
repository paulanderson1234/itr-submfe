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

import java.net.URLEncoder

import auth.{Enrolment, Identifier, MockAuthConnector, MockConfig}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import helpers.FakeRequestHelper
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class OperatingCostsControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper  {

  val mockS4lConnector = mock[S4LConnector]
  val mockSubmissionConnector = mock[SubmissionConnector]

  object OperatingCostsControllerTest extends OperatingCostsController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val s4lConnector: S4LConnector = mockS4lConnector
    val submissionConnector: SubmissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(OperatingCostsControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(OperatingCostsControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

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

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockS4lConnector)
  }

  "OperatingCostsController" should {
    "use the correct keystore connector" in {
      OperatingCostsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      OperatingCostsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      OperatingCostsController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to OperatingCostsController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSaved10PercBoundaryOC)))
      mockEnrolledRequest
      showWithSessionAndAuth(OperatingCostsControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore when authenticated and enrolled" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      showWithSessionAndAuth(OperatingCostsControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to OperatingCostsController when authenticated and NOT enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSaved10PercBoundaryOC)))
      mockNotEnrolledRequest
      showWithSessionAndAuth(OperatingCostsControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to OperatingCostsController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(OperatingCostsControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to OperatingCostsController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(OperatingCostsControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to OperatingCostsController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(OperatingCostsControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a valid form submit to the OperatingCostsController when authenticated and enrolled" should {
    "redirect to the Percentage Of Staff With Masters page (for now)" in {
      when(mockSubmissionConnector.validateKiCostConditions(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(true)))
      when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      mockEnrolledRequest
      val formInput = Seq(
        "operatingCosts1stYear" -> "1000",
        "operatingCosts2ndYear" -> "1000",
        "operatingCosts3rdYear" -> "1000",
        "rAndDCosts1stYear" -> "100",
        "rAndDCosts2ndYear" -> "100",
        "rAndDCosts3rdYear" -> "100"
      )

      submitWithSessionAndAuth(OperatingCostsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/percentage-of-staff-with-masters")
        }
      )
    }
  }

  "Sending a valid form submit to the OperatingCostsController but not KI when authenticated and enrolled" should {
    "redirect to the Ineligible For KI page" in {
      when(mockSubmissionConnector.validateKiCostConditions(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      mockEnrolledRequest
      val formInput = Seq(
        "operatingCosts1stYear" -> "1000",
        "operatingCosts2ndYear" -> "1000",
        "operatingCosts3rdYear" -> "1000",
        "rAndDCosts1stYear" -> "100",
        "rAndDCosts2ndYear" -> "100",
        "rAndDCosts3rdYear" -> "100"
      )

      submitWithSessionAndAuth(OperatingCostsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/ineligible-for-knowledge-intensive")
        }
      )
    }
  }

  "Sending a invalid form submit to the OperatingCostsController when authenticated and enrolled" should {
    "return a bad request" in {

      when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      mockEnrolledRequest

      val formInput = Seq(
        "operatingCosts1stYear" -> "0",
        "operatingCosts2ndYear" -> "0",
        "operatingCosts3rdYear" -> "0",
        "rAndDCosts1stYear" -> "0",
        "rAndDCosts2ndYear" -> "0",
        "rAndDCosts3rdYear" -> "0"
      )

      submitWithSessionAndAuth(OperatingCostsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending an empty KI Model to the OperatingCostsController when authenticated and enrolled" should {
    "redirect to DateOfIncorporation page" in {
      when(mockSubmissionConnector.validateKiCostConditions(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(emptyKIModel)))
      mockEnrolledRequest
      val formInput = Seq(
        "operatingCosts1stYear" -> "100",
        "operatingCosts2ndYear" -> "100",
        "operatingCosts3rdYear" -> "100",
        "rAndDCosts1stYear" -> "10",
        "rAndDCosts2ndYear" -> "10",
        "rAndDCosts3rdYear" -> "10"
      )

      submitWithSessionAndAuth(OperatingCostsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending a KI Model set as None to the OperatingCostsController when authenticated and enrolled" should {
    "redirect to DateOfIncorporation page" in {
      when(mockSubmissionConnector.validateKiCostConditions(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      val formInput = Seq(
        "operatingCosts1stYear" -> "100",
        "operatingCosts2ndYear" -> "100",
        "operatingCosts3rdYear" -> "100",
        "rAndDCosts1stYear" -> "10",
        "rAndDCosts2ndYear" -> "10",
        "rAndDCosts3rdYear" -> "10"
      )

      submitWithSessionAndAuth(OperatingCostsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending an KI Model with missing data to the OperatingCostsController when authenticated and enrolled" should {
    "redirect to DateOfIncorporation page" in {
      when(mockSubmissionConnector.validateKiCostConditions(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(missingKIModel)))
      mockEnrolledRequest
      val formInput = Seq(
        "operatingCosts1stYear" -> "100",
        "operatingCosts2ndYear" -> "100",
        "operatingCosts3rdYear" -> "100",
        "rAndDCosts1stYear" -> "10",
        "rAndDCosts2ndYear" -> "10",
        "rAndDCosts3rdYear" -> "10"
      )

      submitWithSessionAndAuth(OperatingCostsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending an non KI Model to the OperatingCostsController when authenticated and enrolled" should {
    "redirect to IsKI page" in {
      when(mockSubmissionConnector.validateKiCostConditions(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.operatingCosts), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      mockEnrolledRequest
      val formInput = Seq(
        "operatingCosts1stYear" -> "100",
        "operatingCosts2ndYear" -> "100",
        "operatingCosts3rdYear" -> "100",
        "rAndDCosts1stYear" -> "0",
        "rAndDCosts2ndYear" -> "0",
        "rAndDCosts3rdYear" -> "0"
      )

      submitWithSessionAndAuth(OperatingCostsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/is-knowledge-intensive")
        }
      )
    }
  }

  "Sending an empty invalid form submission with validation errors to the CommercialSaleController when authenticated and enrolled" should {
    "return a bad request" in {

      when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSaved10PercBoundaryOC)))
      mockEnrolledRequest
      val formInput = Seq(
        "operatingCosts1stYear" -> " ",
        "operatingCosts2ndYear" -> " ",
        "operatingCosts3rdYear" -> " ",
        "rAndDCosts1stYear" -> " ",
        "rAndDCosts2ndYear" -> " ",
        "rAndDCosts3rdYear" -> " "
      )

      submitWithSessionAndAuth(OperatingCostsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
          //redirectLocation(result) shouldBe Some(routes.OperatingCostsController.show().toString())
        }
      )
    }
  }


  "Sending an invalid form with missing data submission with validation errors to the OperatingCostsController when authenticated and enrolled" should {
    "return a bad request" in {
      mockEnrolledRequest
      val formInput = Seq(
        "operatingCosts1stYear" -> "230000",
        "operatingCosts2ndYear" -> "189250",
        "operatingCosts3rdYear" -> "300000",
        "rAndDCosts1stYear" -> "",
        "rAndDCosts2ndYear" -> "",
        "rAndDCosts3rdYear" -> "")

      submitWithSessionAndAuth(OperatingCostsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
          //redirectLocation(result) shouldBe Some(routes.OperatingCostsController.show().toString())
        }
      )
    }
  }

  "Sending an invalid form with invalid data submission with validation errors to the OperatingCostsController when authenticated and enrolled" should {
    "return a bad request" in {
      mockEnrolledRequest
      val formInput = Seq(
        "operatingCosts1stYear" -> "230000",
        "operatingCosts2ndYear" -> "189250",
        "operatingCosts3rdYear" -> "300000",
        "rAndDCosts1stYear" -> "aaaaa",
        "rAndDCosts2ndYear" -> "10000",
        "rAndDCosts3rdYear" -> "12000")

      submitWithSessionAndAuth(OperatingCostsControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
          //redirectLocation(result) shouldBe Some(routes.OperatingCostsController.show().toString())
        }
      )
    }
  }

  "Sending a submission to the ContactDetailsController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(OperatingCostsControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(OperatingCostsControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the ContactDetailsController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(OperatingCostsControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the ContactDetailsController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      submitWithSessionAndAuth(OperatingCostsControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

}
