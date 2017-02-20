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

package controllers.eisseis

import java.net.URLEncoder
import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class PercentageStaffWithMastersControllerSpec extends BaseSpec {

  object TestController extends PercentageStaffWithMastersController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "PercentageStaffWithMastersController" should {
    "use the correct keystore connector" in {
      PercentageStaffWithMastersController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      OperatingCostsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      OperatingCostsController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct submission connector" in {
      OperatingCostsController.submissionConnector shouldBe SubmissionConnector
    }
  }

  def setupShowMocks(validKI: Option[Boolean] = None, percentageStaffWithMastersModel: Option[PercentageStaffWithMastersModel] = None): Unit = {
    when(mockSubmissionConnector.validateSecondaryKiConditions(Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(validKI))
    when(mockS4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(percentageStaffWithMastersModel))
  }

  def setupSubmitMocks(validKI: Option[Boolean] = None, kiProcessingModel: Option[KiProcessingModel] = None): Unit = {
    when(mockSubmissionConnector.validateSecondaryKiConditions(Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(validKI))
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(kiProcessingModel))
  }

  "Sending a GET request to PercentageStaffWithMastersController when Authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(false),Some(percentageStaffWithMastersModelYes))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore when Authenticated and enrolled" in {
      setupShowMocks(Some(false))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid 'Yes' form submit to the PercentageStaffWithMastersController when Authenticated and enrolled" should {
    "redirect to the subsidiaries page" in {
      setupSubmitMocks(Some(true), Some(trueKIModel))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = "staffWithMasters" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eisseis/subsidiaries")
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit with falseKi in the KI Model to the PercentageStaffWithMastersController when Authenticated and enrolled" should {
    "redirect to the isKI page" in {
      setupSubmitMocks(Some(false), Some(isKiKIModel))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = "staffWithMasters" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eisseis/is-knowledge-intensive")
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit without a KI Model to the PercentageStaffWithMastersController when Authenticated and enrolled" should {
    "redirect to the date of incorporation page" in {
      setupSubmitMocks(Some(false))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = "staffWithMasters" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eisseis/date-of-incorporation")
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit with missing data in the KI Model to the PercentageStaffWithMastersController when Authenticated and enrolled" should {
    "redirect to the date of incorporation page" in {
      setupSubmitMocks(Some(false), Some(missingDataKIModel))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = "staffWithMasters" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eisseis/date-of-incorporation")
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the PercentageStaffWithMastersController when Authenticated and enrolled" should {
    "redirect the ten year plan page" in {
      setupSubmitMocks(Some(false), Some(trueKIModel))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = "staffWithMasters" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eisseis/ten-year-plan")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the PercentageStaffWithMastersController when Authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = "staffWithMasters" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
