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

import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.KeystoreConnector
import controllers.helpers.FakeRequestHelper
import org.mockito.Matchers
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

import scala.concurrent.Future

class SupportingDocumentsControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication with FakeRequestHelper {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object SupportingDocumentsControllerTest extends SupportingDocumentsController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  private def mockBackLinkSetup(backLink: Option[String]) = {
    when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSupportingDocs))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backLink))
  }

  implicit val hc = HeaderCarrier()

  "SupportingDocumentsController" should {
    "use the correct keystore connector" in {
      SupportingDocumentsController.keyStoreConnector shouldBe KeystoreConnector
    }
    "use the correct auth connector" in {
      SupportingDocumentsController.authConnector shouldBe FrontendAuthConnector
    }
  }

  "Sending a GET request to SupportingDocumentsController" should {
    "return a 200 OK" in {
      mockBackLinkSetup(Some(routes.ConfirmCorrespondAddressController.show().url))
      showWithSessionAndAuth(SupportingDocumentsControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "sending a Get requests to the SupportingDocumentsController" should {
    "redirect to the confirm correspondence address page if no saved back link was found" in {
      mockBackLinkSetup(None)
      showWithSessionAndAuth(SupportingDocumentsControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/confirm-correspondence-address")
        }
      )
    }
  }

  "Posting to the SupportingDocumentsController" should {
    "redirect to Check your answers page" in {
      submitWithSessionAndAuth(SupportingDocumentsControllerTest.submit){
        result => status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some("/investment-tax-relief/check-your-answers")
      }
    }
  }

  "Sending a request with no session to SupportingDocumentsController" should {
    "return a 303" in {
      status(SupportingDocumentsControllerTest.show(fakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(SupportingDocumentsControllerTest.show(fakeRequest)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending an Unauthenticated request with a session to SupportingDocumentsController" should {
    "return a 303" in {
      status(SupportingDocumentsControllerTest.show(fakeRequestWithSession)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(SupportingDocumentsControllerTest.show(fakeRequestWithSession)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending a timed-out request to SupportingDocumentsController" should {

    "return a 303 in" in {
      status(SupportingDocumentsControllerTest.show(timedOutFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to timeout page" in {
      redirectLocation(SupportingDocumentsControllerTest.show(timedOutFakeRequest)) shouldBe Some(routes.TimeoutController.timeout().url)
    }
  }

  "Sending a submission to the SupportingDocumentsController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(SupportingDocumentsControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the SupportingDocumentsController with no session" should {

    "redirect to the GG login page with no session" in {
      submitWithoutSession(SupportingDocumentsControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the SupportingDocumentsController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(SupportingDocumentsControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }
}
