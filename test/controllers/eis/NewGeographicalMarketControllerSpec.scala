/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.eis

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
<<<<<<< HEAD:test/controllers/eis/NewGeographicalMarketControllerSpec.scala
import controllers.helpers.ControllerSpec
=======
import helpers.BaseSpec
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/NewGeographicalMarketControllerSpec.scala
import models.NewGeographicalMarketModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class NewGeographicalMarketControllerSpec extends BaseSpec {

  object TestController extends NewGeographicalMarketController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "NewGeographicalMarketController" should {
    "use the correct keystore connector" in {
      NewGeographicalMarketController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      NewGeographicalMarketController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      NewGeographicalMarketController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(newGeographicalMarketModel: Option[NewGeographicalMarketModel] = None, backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(newGeographicalMarketModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkNewGeoMarket))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backLink))
  }

  "Sending a GET request to NewGeographicalMarketController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(newGeographicalMarketModelYes), Some(routes.ProposedInvestmentController.show().url))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore when authenticated and enrolled"  in {
      setupMocks(backLink = Some(routes.ProposedInvestmentController.show().url))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 300 when no back link is fetched using keystore when authenticated and enrolled" in {
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/proposed-investment")
        }
      )
    }
  }

<<<<<<< HEAD:test/controllers/eis/NewGeographicalMarketControllerSpec.scala
  "Sending a GET request to NewGeographicalMarketController when authenticated and NOT enrolled" should {
    "redirect to the Subscription Service" in {
      setupMocks(Some(newGeographicalMarketModelYes), Some(routes.ProposedInvestmentController.show().url))
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to NewGeographicalMarketController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to NewGeographicalMarketController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to NewGeographicalMarketController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

=======
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/NewGeographicalMarketControllerSpec.scala
  "Sending a valid 'Yes' form submit to the NewGeographicalMarketController when authenticated and enrolled" should {
    "redirect to the subsidiaries page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isNewGeographicalMarket" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/new-product")
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the NewGeographicalMarketController when authenticated and enrolled" should {
    "redirect the ten year plan page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isNewGeographicalMarket" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/new-product")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the NewGeographicalMarketController " +
    "with no backlink and when authenticated and enrolled" should {
    "redirect to WhatWillUseFor page" in {
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isNewGeographicalMarket" -> ""
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/proposed-investment")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the NewGeographicalMarketController when authenticated and enrolled" should {
    "redirect to itself with errors" in {
      setupMocks(backLink = Some(routes.ProposedInvestmentController.show().url))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isNewGeographicalMarket" -> ""
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

<<<<<<< HEAD:test/controllers/eis/NewGeographicalMarketControllerSpec.scala
  "Sending a submission to the NewGeographicalMarketController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(TestController.submit)(
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
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the NewGeographicalMarketController when NOT enrolled" should {
    "redirect to the Timeout page when session has timed out" in {
      mockNotEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

=======
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/NewGeographicalMarketControllerSpec.scala
}
