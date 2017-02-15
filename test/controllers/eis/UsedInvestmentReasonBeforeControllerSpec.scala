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
import common.Constants
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
<<<<<<< HEAD:test/controllers/eis/UsedInvestmentReasonBeforeControllerSpec.scala
import controllers.helpers.ControllerSpec
=======
import helpers.BaseSpec
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/UsedInvestmentReasonBeforeControllerSpec.scala
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class UsedInvestmentReasonBeforeControllerSpec extends BaseSpec {

  object TestController extends UsedInvestmentReasonBeforeController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(usedInvestmentReasonBeforeModel: Option[UsedInvestmentReasonBeforeModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(usedInvestmentReasonBeforeModel))

  "UsedInvestmentReasonBeforeController" should {
    "use the correct keystore connector" in {
      UsedInvestmentReasonBeforeController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      UsedInvestmentReasonBeforeController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      UsedInvestmentReasonBeforeController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to UsedInvestmentReasonBeforeController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(usedInvestmentReasonBeforeModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid 'Yes' form submit to the UsedInvestmentReasonBeforeController when authenticated and enrolled" should {
    "redirect to the subsidiaries page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "usedInvestmentReasonBefore" -> Constants.StandardRadioButtonYesValue)(
        result => {
          status(result) shouldBe SEE_OTHER
<<<<<<< HEAD:test/controllers/eis/UsedInvestmentReasonBeforeControllerSpec.scala
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/previous-before-dofcs")
=======
          redirectLocation(result) shouldBe Some(routes.PreviousBeforeDOFCSController.show().url)
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/UsedInvestmentReasonBeforeControllerSpec.scala
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the UsedInvestmentReasonBeforeController when authenticated and enrolled" should {
    "redirect the ten year plan page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "usedInvestmentReasonBefore" -> Constants.StandardRadioButtonNoValue)(
        result => {
          status(result) shouldBe SEE_OTHER
<<<<<<< HEAD:test/controllers/eis/UsedInvestmentReasonBeforeControllerSpec.scala
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/new-geographical-market")
=======
          redirectLocation(result) shouldBe Some(routes.NewGeographicalMarketController.show().url)
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/UsedInvestmentReasonBeforeControllerSpec.scala
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the UsedInvestmentReasonBeforeController" should {
    "redirect to itself" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "usedInvestmentReasonBefore" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

<<<<<<< HEAD:test/controllers/eis/UsedInvestmentReasonBeforeControllerSpec.scala
  "Sending a request with no session to UsedInvestmentReasonBeforeController" should {
    "return a 303" in {
      status(TestController.show(fakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(TestController.show(fakeRequest)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending an Unauthenticated request with a session to UsedInvestmentReasonBeforeController" should {
    "return a 303" in {
      status(TestController.show(fakeRequestWithSession)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(TestController.show(fakeRequestWithSession)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending a timed-out request to UsedInvestmentReasonBeforeController" should {

    "return a 303 in" in {
      status(TestController.show(timedOutFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to timeout page" in {
      redirectLocation(TestController.show(timedOutFakeRequest)) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
    }
  }

  "Sending a request to UsedInvestmentReasonBeforeController when NOT enrolled" should {

    "return a 303 in" in {
      mockNotEnrolledRequest()
      status(TestController.show(authorisedFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to Subscription Service" in {
      mockNotEnrolledRequest()
      redirectLocation(TestController.show(authorisedFakeRequest)) shouldBe Some(FrontendAppConfig.subscriptionUrl)
    }
  }

  "Sending a submission to the UsedInvestmentReasonBeforeController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the UsedInvestmentReasonBeforeController with no session" should {

    "redirect to the GG login page with no session" in {
      submitWithoutSession(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the UsedInvestmentReasonBeforeController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the UsedInvestmentReasonBeforeController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
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
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/UsedInvestmentReasonBeforeControllerSpec.scala
}
