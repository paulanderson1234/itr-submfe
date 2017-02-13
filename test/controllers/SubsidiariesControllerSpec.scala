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

package controllers

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class SubsidiariesControllerSpec extends BaseSpec {

  object TestController extends SubsidiariesController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(backLink: Option[String] = None, subsidiariesModel: Option[SubsidiariesModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[String]
      (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(subsidiariesModel))
  }

  "SubsidiariesController" should {
    "use the correct keystore connector" in {
      SubsidiariesController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      SubsidiariesController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      SubsidiariesController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to SubsidiariesController without a valid back link from keystore when authenticated and enrolled" should {
    "redirect to the beginning of the flow" in {
      setupMocks(subsidiariesModel = Some(subsidiariesModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        }
      )
    }
  }

  "Sending a GET request to SubsidiariesController when authenticated and enrolled" should {
    "return a 303" in {
      setupMocks(Some(routes.TenYearPlanController.show().url), Some(subsidiariesModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe SEE_OTHER
      )
    }

    "redirect to the HadPreviousRFI page" in {
      setupMocks(Some(routes.TenYearPlanController.show().url), Some(subsidiariesModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
      )
    }
//    "return a 200 when something is fetched from keystore" in {
//      setupMocks(Some(routes.TenYearPlanController.show().url), Some(subsidiariesModelYes))
//      mockEnrolledRequest(eisSchemeTypesModel)
//      showWithSessionAndAuth(TestController.show)(
//        result => status(result) shouldBe OK
//      )
//    }
//
//    "provide an empty model and return a 200 when nothing is fetched using keystore when authenticated and enrolled" in {
//      setupMocks(Some(routes.PercentageStaffWithMastersController.show().url))
//      mockEnrolledRequest(eisSchemeTypesModel)
//      showWithSessionAndAuth(TestController.show)(
//        result => status(result) shouldBe OK
//      )
//    }
  }

  "Sending a valid 'Yes' form submit to the SubsidiariesController when authenticated and enrolled" should {
    "redirect to the previous investment before page" in {
      setupMocks(Some(routes.TenYearPlanController.show().url))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "subsidiaries" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/used-investment-scheme-before")
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the SubsidiariesController when authenticated and enrolled" should {
    "redirect to the previous investment before page" in {
      setupMocks(Some(routes.TenYearPlanController.show().url))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "subsidiaries" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the SubsidiariesController when authenticated and enrolled" should {
    "redirect to the previous investment before page" in {
      setupMocks(Some(routes.TenYearPlanController.show().url))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "subsidiaries" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }
//    "redirect to itself with errors" in {
//      setupMocks(Some(routes.TenYearPlanController.show().url))
//      mockEnrolledRequest(eisSchemeTypesModel)
//      val formInput = "ownSubsidiaries" -> ""
//      submitWithSessionAndAuth(TestController.submit, formInput)(
//        result => {
//          status(result) shouldBe BAD_REQUEST
//        }
//      )
//    }
  }

}
