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
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, KeystoreConnector, SubmissionConnector}
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.mock.MockitoSugar
import common.Constants.{StandardRadioButtonNoValue, StandardRadioButtonYesValue}
import controllers.helpers.FakeRequestHelper

import scala.concurrent.Future

class ProposedInvestmentControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper{

  val mockKeyStoreConnector = mock[KeystoreConnector]
  val mockSubmissionConnector = mock[SubmissionConnector]

  object ProposedInvestmentControllerTest extends ProposedInvestmentController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
    val submissionConnector: SubmissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(ProposedInvestmentControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(ProposedInvestmentControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

  val model1 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeEisValue, 2356, None, None, Some(4), Some(12), Some(2009), Some(1))
  val model2 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeSeisValue, 2356, Some(666), None, Some(4), Some(12), Some(2010), Some(3))
  val model3 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 2356, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model4 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 19999999, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model5 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 1, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model6 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 11999999, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model7 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 15000000, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))


  val emptyVectorList = Vector[PreviousSchemeModel]()
  val previousSchemeTrueKIVectorList = Vector(model1, model2, model3)
  val previousSchemeOverTrueKIVectorList = Vector(model4, model5, model5)
  val previousSchemeFalseKIVectorList = Vector(model1, model2, model3)
  val previousSchemeOverFalseKIVectorList = Vector(model4, model5, model6)
  val previousSchemeUnderTotalAmount = Vector(model3, model5, model7)


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

  val keyStoreSavedhadPreviousRFIModel = HadPreviousRFIModel(StandardRadioButtonYesValue)
  val keyStoreSavednoPreviousRFIModel = HadPreviousRFIModel(StandardRadioButtonNoValue)


  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "ProposedInvestmentController" should {
    "use the correct keystore connector" in {
      ProposedInvestmentController.keyStoreConnector shouldBe KeystoreConnector
    }
    "use the correct auth connector" in {
      ProposedInvestmentController.authConnector shouldBe FrontendAuthConnector
    }
  }

  "Sending a GET request to ProposedInvestmentController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel]
        (Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedProposedInvestment)))
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavednoPreviousRFIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      mockEnrolledRequest
      showWithSessionAndAuth(ProposedInvestmentControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore when authenticated and enrolled" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)

      when(mockKeyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel]
        (Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      mockEnrolledRequest
      showWithSessionAndAuth(ProposedInvestmentControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to ProposedInvestmentController when authenticated and NOT enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel]
        (Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedProposedInvestment)))
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavednoPreviousRFIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      mockNotEnrolledRequest
      showWithSessionAndAuth(ProposedInvestmentControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending a GET request to ProposedInvestmentController without a valid backlink from keystore when authenticated and enrolled" should {
    "redirect to the beginning of the flow" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      showWithSessionAndAuth(ProposedInvestmentControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/used-investment-scheme-before")
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to ProposedInvestmentController when authenticated and enrolled" should {
    "return a 302 and redirect to GG login" in {
      mockEnrolledRequest
      showWithSessionWithoutAuth(ProposedInvestmentControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to ProposedInvestmentController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(ProposedInvestmentControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to ProposedInvestmentController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(ProposedInvestmentControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a valid form submit with not exceeding the lifetime allowance (true KI) to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the what will use for page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedhadPreviousRFIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(ProposedInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit with a not exceeding the lifetime allowance (true KI) and no previous RFI to the ProposedInvestmentController" +
    "when authenticated and enrolled" should {
    "redirect to the what will use for page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavednoPreviousRFIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(ProposedInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit with exceeded lifetime allowance (true KI) to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the exceeded lifetime limit page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(true)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedhadPreviousRFIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeOverTrueKIVectorList)))
      mockEnrolledRequest
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(ProposedInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/lifetime-allowance-exceeded")
        }
      )
    }
  }

  "Sending a valid form submit with not exceeding the lifetime allowance (false KI) to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the what will do page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedhadPreviousRFIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeFalseKIVectorList)))
      mockEnrolledRequest
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(ProposedInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending a valid form submit with exceeded lifetime allowance (false KI) to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the exceeded lifetime limit page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(true)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedhadPreviousRFIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeOverFalseKIVectorList)))
      mockEnrolledRequest
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(ProposedInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/lifetime-allowance-exceeded")
        }
      )
    }
  }

  "Sending a valid form submit with not exceeded lifetime allowance (false KI) to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the exceeded lifetime limit page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(true)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedhadPreviousRFIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeUnderTotalAmount)))
      mockEnrolledRequest
      val formInput = "investmentAmount" -> "5000000"
      submitWithSessionAndAuth(ProposedInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/lifetime-allowance-exceeded")
        }
      )
    }
  }

  "Sending a valid form submit with No KIModel to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the DateOfIncorporation page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedhadPreviousRFIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(ProposedInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect with a bad request" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      mockEnrolledRequest
      val formInput = "investmentAmount" -> "fff"
      submitWithSessionAndAuth(ProposedInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending an invalid form submission with value 0 to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect with a bad request" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      mockEnrolledRequest
      val formInput = "investmentAmount" -> "0"
      submitWithSessionAndAuth(ProposedInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending an invalid form submission with value 5000001 to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect with a bad request" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      mockEnrolledRequest
      val formInput = "investmentAmount" -> "5000001"
      submitWithSessionAndAuth(ProposedInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a submission to the NewGeographicalMarketController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(ProposedInvestmentControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(ProposedInvestmentControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the NewGeographicalMarketController when a timeout has occurred" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(ProposedInvestmentControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the NewGeographicalMarketController when NOT enrolled" should {
    "redirect to the Subscription Servicec" in {
      mockNotEnrolledRequest
      submitWithSessionAndAuth(ProposedInvestmentControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

}
