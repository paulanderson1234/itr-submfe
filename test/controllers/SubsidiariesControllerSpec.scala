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
import connectors.{EnrolmentConnector, KeystoreConnector}
import controllers.helpers.FakeRequestHelper
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

import scala.concurrent.Future

class SubsidiariesControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object SubsidiariesControllerTest extends SubsidiariesController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(SubsidiariesControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(SubsidiariesControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

  val modelYes = SubsidiariesModel(Constants.StandardRadioButtonYesValue)
  val modelNo = SubsidiariesModel(Constants.StandardRadioButtonNoValue)
  val emptyModel = SubsidiariesModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(modelYes)))
  val keyStoreSavedSubsidiaries = SubsidiariesModel(Constants.StandardRadioButtonYesValue)

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "SubsidiariesController" should {
    "use the correct keystore connector" in {
      SubsidiariesController.keyStoreConnector shouldBe KeystoreConnector
    }
    "use the correct auth connector" in {
      SubsidiariesController.authConnector shouldBe FrontendAuthConnector
    }
  }

  "Sending a GET request to SubsidiariesController without a valid back link from keystore when authenticated and enrolled" should {
    "redirect to the beginning of the flow" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiaries)))
      mockEnrolledRequest
      showWithSessionAndAuth(SubsidiariesControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending a GET request to SubsidiariesController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.TenYearPlanController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiaries)))
      mockEnrolledRequest
      showWithSessionAndAuth(SubsidiariesControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore when authenticated and enrolled" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.PercentageStaffWithMastersController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      showWithSessionAndAuth(SubsidiariesControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to SubsidiariesController when authenticated and NOT enrolled" should {
    "redirect to the Subscription Service" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.TenYearPlanController.show().toString())))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiaries)))
      mockNotEnrolledRequest
      showWithSessionAndAuth(SubsidiariesControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to SubsidiariesController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(SubsidiariesControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to SubsidiariesController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(SubsidiariesControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to SubsidiariesController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(SubsidiariesControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the SubsidiariesController when authenticated and enrolled" should {
    "redirect to the previous investment before page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.TenYearPlanController.show().toString())))
      when(mockKeyStoreConnector.saveFormData(Matchers.eq(KeystoreKeys.subsidiaries), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      mockEnrolledRequest
      val formInput = "subsidiaries" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(SubsidiariesControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/used-investment-scheme-before")
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the SubsidiariesController when authenticated and enrolled" should {
    "redirect to the previous investment before page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.TenYearPlanController.show().toString())))
      when(mockKeyStoreConnector.saveFormData(Matchers.eq(KeystoreKeys.subsidiaries), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      mockEnrolledRequest
      val formInput = "subsidiaries" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(SubsidiariesControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/used-investment-scheme-before")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the SubsidiariesController when authenticated and enrolled" should {
    "redirect to itself with errors" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.TenYearPlanController.show().toString())))
      mockEnrolledRequest
      val formInput = "ownSubsidiaries" -> ""
      submitWithSessionAndAuth(SubsidiariesControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a submission to the SubsidiariesController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(SubsidiariesControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(SubsidiariesControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the SubsidiariesController when a timeout has occurred" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(SubsidiariesControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the SubsidiariesController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      submitWithSessionAndAuth(SubsidiariesControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }
}
