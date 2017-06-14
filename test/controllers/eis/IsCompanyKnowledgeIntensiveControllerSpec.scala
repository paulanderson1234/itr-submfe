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
import controllers.helpers.BaseSpec
import models.{IsCompanyKnowledgeIntensiveModel, KiProcessingModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class IsCompanyKnowledgeIntensiveControllerSpec extends BaseSpec {

  object TestController extends IsCompanyKnowledgeIntensiveController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val updatedKIModel = KiProcessingModel(Some(true),Some(false), Some(false), Some(false), None, Some(false))
  val missingDateKIModel = KiProcessingModel(Some(true),None, Some(false), Some(false), None, Some(false))

  def setupShowMocks(isCompanyKnowledgeIntensiveModel: Option[IsCompanyKnowledgeIntensiveModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[IsCompanyKnowledgeIntensiveModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(isCompanyKnowledgeIntensiveModel))

  def setupSubmitMocks(kiProcessingModel: Option[KiProcessingModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(kiProcessingModel))

  "IsCompanyKnowledgeIntensiveController" should {
    "use the correct keystore connector" in {
      IsCompanyKnowledgeIntensiveController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      IsCompanyKnowledgeIntensiveController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      IsCompanyKnowledgeIntensiveController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to IsCompanyKnowledgeIntensiveController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(isCompanyKnowledgeIntensiveModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupShowMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid 'Yes' form submit to the IsCompanyKnowledgeIntensiveController when authenticated and enrolled" should {
    "redirect to the Do you want to apply for KI page" in {
      setupSubmitMocks(Some(updatedKIModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isCompanyKnowledgeIntensive" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.IsKnowledgeIntensiveController.show().url)
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit with missing data in the KI Model to the IsCompanyKnowledgeIntensiveController when authenticated and enrolled" should {
    "redirect to the date of incorporation page" in {
      setupSubmitMocks(Some(missingDateKIModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isCompanyKnowledgeIntensive" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit with a false KI Model to the IsCompanyKnowledgeIntensiveController when authenticated and enrolled" should {
    "redirect to the subsidiaries" in {
      setupSubmitMocks(Some(falseKIModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isCompanyKnowledgeIntensive" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit without a KI Model to the IsCompanyKnowledgeIntensiveController when authenticated and enrolled" should {
    "redirect to the date of incorporation" in {
      setupSubmitMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isCompanyKnowledgeIntensive" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the IsCompanyKnowledgeIntensiveController when authenticated and enrolled" should {
    "redirect to the subsidiaries" in {
      setupSubmitMocks(Some(updatedKIModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isCompanyKnowledgeIntensive" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the IsCompanyKnowledgeIntensiveController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isCompanyKnowledgeIntensive" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
