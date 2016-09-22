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
import java.util.UUID

import auth.{MockAuthConnector, MockConfig}
import builders.SessionBuilder
import common.{Constants, KeystoreKeys}
import config.FrontendAppConfig
import connectors.{KeystoreConnector, SubmissionConnector}
import controllers.helpers.FakeRequestHelper
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

class TenYearPlanControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper {

  val mockKeyStoreConnector = mock[KeystoreConnector]
  val mockSubmissionConnector = mock[SubmissionConnector]

  object TenYearPlanControllerTest extends TenYearPlanController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
    val submissionConnector: SubmissionConnector = mockSubmissionConnector
  }

  val model = TenYearPlanModel(Constants.StandardRadioButtonYesValue, Some("Text"))
  val emptyModel = TenYearPlanModel("", None)
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val keyStoreSavedYesWithTenYearPlan = TenYearPlanModel(Constants.StandardRadioButtonYesValue, Some("abcd"))
  val keyStoreSavedNoWithNoTenYearPlan = TenYearPlanModel(Constants.StandardRadioButtonNoValue, None)
  val trueKIModel = KiProcessingModel(Some(true), Some(true), Some(true), Some(true), None, Some(true))
  val falseKIModel = KiProcessingModel(Some(true), Some(false), Some(false), Some(false), None, Some(false))
  val isKiKIModel = KiProcessingModel(Some(false), Some(true), Some(true), Some(true), Some(true), Some(true))
  val noMastersKIModel = KiProcessingModel(Some(true), Some(true), Some(true), None, Some(true), Some(true))
  val emptyKIModel = KiProcessingModel(None, None, None, None, None, None)

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "TenYearPlanController" should {
    "use the correct keystore connector" in {
      TenYearPlanController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to TenYearPlanController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedYesWithTenYearPlan)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      showWithSessionAndAuth(TenYearPlanControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyKIModel)))
      showWithSessionAndAuth(TenYearPlanControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

  }

  "Sending a valid No form submission to the TenYearPlanController with a false KI Model" should {
    "redirect to the subsidiaries page if no and and no description" in {
      when(mockSubmissionConnector.validateSecondaryKiConditions(Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(isKiKIModel)))
      submitWithSessionAndAuth(TenYearPlanControllerTest.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonNoValue,
        "tenYearPlanDesc" -> "")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/is-knowledge-intensive")
        }
      )
    }
  }

  "Sending a valid No form submission to the TenYearPlanController without a KI Model" should {
    "redirect to the subsidiaries page if no and and no description" in {
      when(mockSubmissionConnector.validateSecondaryKiConditions(Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      submitWithSessionAndAuth(TenYearPlanControllerTest.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonNoValue,
        "tenYearPlanDesc" -> "")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending a valid No form submission to the TenYearPlanController without hasPercentageWithMasters in the KI Model" should {
    "redirect to the subsidiaries page if no and and no description" in {
      when(mockSubmissionConnector.validateSecondaryKiConditions(Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(noMastersKIModel)))
      submitWithSessionAndAuth(TenYearPlanControllerTest.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonNoValue,
        "tenYearPlanDesc" -> "")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending a valid No form submission to the TenYearPlanController" should {
    "redirect to the subsidiaries page if no and and no description" in {
      when(mockSubmissionConnector.validateSecondaryKiConditions(Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      submitWithSessionAndAuth(TenYearPlanControllerTest.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonNoValue,
        "tenYearPlanDesc" -> "")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/ineligible-for-knowledge-intensive")
        }
      )
    }
  }

  "Sending a valid Yes form submission to the TenYearPlanController" should {
    "redirect to the subsidiaries page with valid submission" in {
      when(mockSubmissionConnector.validateSecondaryKiConditions(Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(true)))
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      submitWithSessionAndAuth(TenYearPlanControllerTest.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonYesValue,
        "tenYearPlanDesc" -> "text")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }

  "Sending an empty invalid form submission with validation errors to the TenYearPlanController" should {
    "redirect to itself" in {
      submitWithSessionAndAuth(TenYearPlanControllerTest.submit,
        "hasTenYearPlan" -> "",
        "tenYearPlanDesc" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
          redirectLocation(result) shouldBe None
        }
      )
    }
  }

  "Sending an an invalid form submission with both Yes and a blank description to the TenYearPlanController" should {
    "redirect to itself with validation errors" in {
      submitWithSessionAndAuth(TenYearPlanControllerTest.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonYesValue,
        "tenYearPlanDesc" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }



  "Sending a request with no session to TenYearPlanController" should {
    "return a 303" in {
      status(TenYearPlanControllerTest.show(fakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(TenYearPlanControllerTest.show(fakeRequest)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending an Unauthenticated request with a session to TenYearPlanController" should {
    "return a 303" in {
      status(TenYearPlanControllerTest.show(fakeRequestWithSession)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(TenYearPlanControllerTest.show(fakeRequestWithSession)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending a timed-out request to TenYearPlanController" should {

    "return a 303 in" in {
      status(TenYearPlanControllerTest.show(timedOutFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to timeout page" in {
      redirectLocation(TenYearPlanControllerTest.show(timedOutFakeRequest)) shouldBe Some(routes.TimeoutController.timeout().url)
    }
  }

  "Sending a submission to the TenYearPlanController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(TenYearPlanControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the TenYearPlanController with no session" should {

    "redirect to the GG login page with no session" in {
      submitWithoutSession(TenYearPlanControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the TenYearPlanController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TenYearPlanControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }
}
