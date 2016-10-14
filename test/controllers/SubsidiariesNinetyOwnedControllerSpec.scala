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
import config.{FrontendAppConfig, FrontendAuthConnector}
import models.SubsidiariesNinetyOwnedModel
import common.Constants
import connectors.{EnrolmentConnector, KeystoreConnector}
import controllers.helpers.FakeRequestHelper
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

class SubsidiariesNinetyOwnedControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper{

  val mockKeyStoreConnector = mock[KeystoreConnector]


  object SubsidiariesNinetyOwnedControllerTest extends SubsidiariesNinetyOwnedController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(SubsidiariesNinetyOwnedControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(SubsidiariesNinetyOwnedControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

  val model = SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonYesValue)
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val keyStoreSavedSubsidiariesNinetyOwned = SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonYesValue)

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "SubsidiariesNinetyOwnedController" should {
    "use the correct keystore connector" in {
      SubsidiariesNinetyOwnedController.keyStoreConnector shouldBe KeystoreConnector
    }
    "use the correct auth connector" in {
      SubsidiariesNinetyOwnedController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      SubsidiariesNinetyOwnedController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to SubsidiariesNinetyOwnedController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNinetyOwned)))
      mockEnrolledRequest
      showWithSessionAndAuth(SubsidiariesNinetyOwnedControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      showWithSessionAndAuth(SubsidiariesNinetyOwnedControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to SubsidiariesNinetyOwnedController when authenticated and NOT enrolled" should {
    "redirect to the Subscription Service" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNinetyOwned)))
      mockNotEnrolledRequest
      showWithSessionAndAuth(SubsidiariesNinetyOwnedControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to SubsidiariesNinetyOwnedController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(SubsidiariesNinetyOwnedControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to SubsidiariesNinetyOwnedController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(SubsidiariesNinetyOwnedControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to SubsidiariesNinetyOwnedController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(SubsidiariesNinetyOwnedControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a valid form submission to the SubsidiariesNinetyOwnedController when authenticated and enrolled" should {
    "redirect to the how-plan-to-use-investment page" in {
      mockEnrolledRequest
      val formInput = "ownNinetyPercent" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(SubsidiariesNinetyOwnedControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/how-plan-to-use-investment")
        }
      )
    }
  }

  "Sending an empty invalid form submission with validation errors to the SubsidiariesNinetyOwnedController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest
      val formInput = "ownNinetyPercent" -> ""
      submitWithSessionAndAuth(SubsidiariesNinetyOwnedControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a submission to the SubsidiariesNinetyOwnedController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(SubsidiariesNinetyOwnedControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(SubsidiariesNinetyOwnedControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the SubsidiariesNinetyOwnedController when a timeout has occurred" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(SubsidiariesNinetyOwnedControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the SubsidiariesNinetyOwnedController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      submitWithSessionAndAuth(SubsidiariesNinetyOwnedControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }
}
