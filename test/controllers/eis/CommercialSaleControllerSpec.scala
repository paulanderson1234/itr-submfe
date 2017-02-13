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

import java.net.URLEncoder

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.ControllerSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class CommercialSaleControllerSpec extends ControllerSpec {

  object TestController extends CommercialSaleController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "CommercialSaleController" should {
    "use the correct auth connector" in {
      CommercialSaleController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      CommercialSaleController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      CommercialSaleController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupShowMocks(commercialSaleModel: Option[CommercialSaleModel] = None): Unit =
  when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
    .thenReturn(Future.successful(commercialSaleModel))

  def setupSubmitMocks(kiProcessingModel: Option[KiProcessingModel] = None, dateOfIncorporationModel: Option[DateOfIncorporationModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(kiProcessingModel))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(dateOfIncorporationModel))
  }

  "Sending a GET request to CommercialSaleController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(commercialSaleModelYes))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupShowMocks()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending an Unauthenticated request with a session to CommercialSaleController" should {
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

  "Sending a request with no session to CommercialSaleController" should {
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

  "Sending a timed-out request to CommercialSaleController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a NOT enrolled request" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending a valid Yes form submission to the CommercialSaleController when authenticated and enrolled" should {
    "redirect to the KI page if the KI date condition is met" in {
      val formInput = Seq("hasCommercialSale" -> Constants.StandardRadioButtonYesValue,
        "commercialSaleDay" -> "23",
        "commercialSaleMonth" -> "11",
        "commercialSaleYear" -> "1993")
      setupSubmitMocks(Some(kiProcessingModelMet), Some(dateOfIncorporationModel))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit,formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/is-knowledge-intensive")
        }
      )
    }
  }

  "Sending a valid Yes form submission to the CommercialSaleController when authenticated and enrolled" should {
    "redirect to the subsidiaries page if the KI date condition is not met" in {
      val formInput = Seq(
        "hasCommercialSale" -> Constants.StandardRadioButtonYesValue,
        "commercialSaleDay" -> "23",
        "commercialSaleMonth" -> "11",
        "commercialSaleYear" -> "1993")
      setupSubmitMocks(Some(kiProcessingModelNotMet), Some(dateOfIncorporationModel))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/subsidiaries")
        }
      )
    }
  }

  "Sending a valid No form submission with a empty KI Model to the CommercialSaleController when authenticated and enrolled" should {
    "redirect to the date of incorporation page" in {
      val formInput = Seq(
        "hasCommercialSale" -> Constants.StandardRadioButtonYesValue,
        "commercialSaleDay" -> "23",
        "commercialSaleMonth" -> "11",
        "commercialSaleYear" -> "1993")
      setupSubmitMocks(dateOfIncorporationModel = Some(dateOfIncorporationModel))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/date-of-incorporation")
        }
      )
    }
  }

  "Sending a valid No form submission to the CommercialSaleController when authenticated and enrolled" should {
    "redirect to the KI page if the KI date condition is met" in {
      val formInput = Seq(
        "hasCommercialSale" -> Constants.StandardRadioButtonNoValue,
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      setupSubmitMocks(Some(kiProcessingModelMet), Some(dateOfIncorporationModel))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/is-knowledge-intensive")
        }
      )
    }
  }

  "Sending a valid No form submission with a Ki Model which has missing data to the CommercialSaleController when authenticated and enrolled" should {
    "redirect to the date of incorporation page" in {
      val formInput = Seq(
        "hasCommercialSale" -> Constants.StandardRadioButtonNoValue,
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      setupSubmitMocks(dateOfIncorporationModel = Some(dateOfIncorporationModel))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/date-of-incorporation")
        }
      )
    }
  }

  "Sending a valid No form submission to the CommercialSaleController when authenticated and enrolled" should {
    "redirect to the subsidiaries page if the KI date condition is not met" in {
      val formInput = Seq(
        "hasCommercialSale" -> Constants.StandardRadioButtonNoValue,
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      setupSubmitMocks(Some(kiProcessingModelNotMet), Some(dateOfIncorporationModel))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/subsidiaries")
        }
      )
    }
  }

  "Sending a submission to the CommercialSaleController when not authenticated" should {

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

  "Sending a submission to the CommercialSaleController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the CommercialSaleController when not enrolled" should {

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

}
