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

import auth.{Enrolment, Identifier, MockAuthConnector, MockConfig}
import builders.SessionBuilder
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, KeystoreConnector}
import controllers.helpers.FakeRequestHelper
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

class YourCompanyNeedControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object YourCompanyNeedControllerTest extends YourCompanyNeedController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(YourCompanyNeedControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(YourCompanyNeedControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

  val modelAA = YourCompanyNeedModel("AA")
  val modelCS = YourCompanyNeedModel("CS")
  val emptyModel = YourCompanyNeedModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(modelAA)))
  val keyStoreSavedYourCompanyNeed = YourCompanyNeedModel("AA")

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "YourCompanyNeedController" should {
    "use the correct keystore connector" in {
      YourCompanyNeedController.keyStoreConnector shouldBe KeystoreConnector
    }
    "use the correct auth connector" in {
      YourCompanyNeedController.authConnector shouldBe FrontendAuthConnector
    }
  }

  "Sending a GET request to YourCompanyNeedController when authenticated and enrolled" should {
    "return a 200 OK Swhen something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedYourCompanyNeed)))
      mockEnrolledRequest
      showWithSessionAndAuth(YourCompanyNeedControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 OK when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      showWithSessionAndAuth(YourCompanyNeedControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid 'Advanced Assurance' option form submit to the YourCompanyNeedController when authenticated and enrolled" should {
    "redirect to the qualifying for a scheme page" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      mockEnrolledRequest
      submitWithSessionAndAuth(YourCompanyNeedControllerTest.submit, "needAAorCS" -> "AA")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/qualifying-for-scheme")
        }
      )
    }
  }

  "Sending a valid 'Compliance Statement' option form submit to the YourCompanyNeedController when authenticated and enrolled" should {
    "redirect to the qualifying for a scheme page" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      mockEnrolledRequest
      submitWithSessionAndAuth(YourCompanyNeedControllerTest.submit, "needAAorCS" -> "CS")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/qualifying-for-scheme")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the YourCompanyNeedController" should {
    "redirect to itself" in {
      mockEnrolledRequest
      submitWithSessionAndAuth(YourCompanyNeedControllerTest.submit,"needAAorCS" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a request with no session to YourCompanyNeedController" should {
    "return a 303" in {
      status(YourCompanyNeedControllerTest.show(fakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(YourCompanyNeedControllerTest.show(fakeRequest)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending an Unauthenticated request with a session to YourCompanyNeedController" should {
    "return a 303" in {
      status(WhatWeAskYouController.show(fakeRequestWithSession)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(YourCompanyNeedControllerTest.show(fakeRequestWithSession)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending a timed-out request to YourCompanyNeedController" should {

    "return a 303 in" in {
      status(YourCompanyNeedControllerTest.show(timedOutFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to timeout page" in {
      redirectLocation(YourCompanyNeedControllerTest.show(timedOutFakeRequest)) shouldBe Some(routes.TimeoutController.timeout().url)
    }
  }

  "Sending a request to YourCompanyNeedController when NOT enrolled" should {
    "return a 303 in" in {
      mockNotEnrolledRequest
      status(YourCompanyNeedControllerTest.show(authorisedFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      redirectLocation(YourCompanyNeedControllerTest.show(authorisedFakeRequest)) shouldBe Some(FrontendAppConfig.subscriptionUrl)
    }
  }

  "Sending a submission to the YourCompanyNeedController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(YourCompanyNeedControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the YourCompanyNeedController with no session" should {

    "redirect to the GG login page with no session" in {
      submitWithoutSession(YourCompanyNeedControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the YourCompanyNeedController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(YourCompanyNeedControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the YourCompanyNeedController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      submitWithSessionAndAuth(YourCompanyNeedControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

}
