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
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
<<<<<<< HEAD:test/controllers/eis/DateOfIncorporationControllerSpec.scala
import controllers.helpers.ControllerSpec
=======
import helpers.BaseSpec
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/DateOfIncorporationControllerSpec.scala
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class DateOfIncorporationControllerSpec extends BaseSpec {

  object DateOfIncorporationControllerTest extends DateOfIncorporationController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(dateOfIncorporationModel: Option[DateOfIncorporationModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(dateOfIncorporationModel))

  "DateOfIncorporationController" should {
    "use the correct keystore connector" in {
      DateOfIncorporationController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      DateOfIncorporationController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      DateOfIncorporationController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to DateOfIncorporationController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(dateOfIncorporationModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(DateOfIncorporationControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(DateOfIncorporationControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }
  }

<<<<<<< HEAD:test/controllers/eis/DateOfIncorporationControllerSpec.scala
  "Sending a GET request to DateOfIncorporationController when authenticated and NOT enrolled" should {
    "redirect to the Subscription Service" in {
      setupMocks(Some(dateOfIncorporationModel))
      mockNotEnrolledRequest()
      showWithSessionAndAuth(DateOfIncorporationControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to DateOfIncorporationController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(DateOfIncorporationControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to DateOfIncorporationController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(DateOfIncorporationControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to DateOfIncorporationController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(DateOfIncorporationControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

=======
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/DateOfIncorporationControllerSpec.scala
  "Sending a valid form submit to the DateOfIncorporationController when authenticated and enrolled" should {
    "redirect to first commercial sale page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))
        (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(Option(kiProcessingModelMet)))
      setupMocks(Some(dateOfIncorporationModel))
      mockEnrolledRequest(eisSchemeTypesModel)

      val formInput = Seq(
        "incorporationDay" -> "23",
        "incorporationMonth" -> "11",
        "incorporationYear" -> "1993")

      submitWithSessionAndAuth(DateOfIncorporationControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CommercialSaleController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the DateOfIncorporationController when authenticated and enrolled" should {
    "return a bad request" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = Seq(
        "incorporationDay" -> "",
        "incorporationMonth" -> "",
        "incorporationYear" -> "")

      submitWithSessionAndAuth(DateOfIncorporationControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

<<<<<<< HEAD:test/controllers/eis/DateOfIncorporationControllerSpec.scala
  "Sending a submission to the DateOfIncorporationController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(DateOfIncorporationControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(DateOfIncorporationControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the DateOfIncorporationController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(DateOfIncorporationControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the DateOfIncorporationController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest()
      submitWithSessionAndAuth(DateOfIncorporationControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

=======
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/DateOfIncorporationControllerSpec.scala
}
