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
import config.{FrontendAppConfig, FrontendAuthConnector}
import controllers.helpers.FakeRequestHelper
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest.mock.MockitoSugar


class QualifyingForSchemeControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication with FakeRequestHelper {

  object QualifyingForSchemeControllerTest extends QualifyingForSchemeController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
  }

  implicit val hc = HeaderCarrier()

  "QualifyingForSchemeController" should {
    "use the correct auth connector" in {
      QualifyingForSchemeController.authConnector shouldBe FrontendAuthConnector
    }
  }

  "Sending a GET request to QualifyingForSchemeController" should {
    "return a 200 OK" in {
      showWithSessionAndAuth(QualifyingForSchemeControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending an Unauthenticated request with a session to NewGeographicalMarketController when authenticated" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(QualifyingForSchemeControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to NewGeographicalMarketController when authenticated" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(QualifyingForSchemeControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to NewGeographicalMarketController when authenticated" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(QualifyingForSchemeControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Posting to the QualifyingForSchemeController when authenticated" should {
    "redirect to 'What we'll ask you' page" in {
      submitWithSessionAndAuth(QualifyingForSchemeControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/what-we-ask-you")
      }
      )
    }
  }

  "Sending a submission to the QualifyingForSchemeController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(QualifyingForSchemeControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(QualifyingForSchemeControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the QualifyingForSchemeController when a timeout has occurred" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(QualifyingForSchemeControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }
}
