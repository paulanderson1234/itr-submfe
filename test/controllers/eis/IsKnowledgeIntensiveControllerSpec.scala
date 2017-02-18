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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.{IsKnowledgeIntensiveModel, KiProcessingModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class IsKnowledgeIntensiveControllerSpec extends BaseSpec {

  object TestController extends IsKnowledgeIntensiveController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val updatedKIModel = KiProcessingModel(Some(true),Some(false), Some(false), Some(false), None, Some(false))
  val missingDateKIModel = KiProcessingModel(Some(true),None, Some(false), Some(false), None, Some(false))

  def setupShowMocks(isKnowledgeIntensiveModel: Option[IsKnowledgeIntensiveModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(isKnowledgeIntensiveModel))

  def setupSubmitMocks(kiProcessingModel: Option[KiProcessingModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(kiProcessingModel))

  "IsKnowledgeIntensiveController" should {
    "use the correct keystore connector" in {
      IsKnowledgeIntensiveController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      IsKnowledgeIntensiveController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      IsKnowledgeIntensiveController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to IsKnowledgeIntensiveController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(isKnowledgeIntensiveModelYes))
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

  "Sending a valid 'Yes' form submit to the IsKnowledgeIntensiveController when authenticated and enrolled" should {
    "redirect to the operating costs page" in {
      setupSubmitMocks(Some(updatedKIModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isKnowledgeIntensive" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.OperatingCostsController.show().url)
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit with missing data in the KI Model to the IsKnowledgeIntensiveController when authenticated and enrolled" should {
    "redirect to the date of incorporation page" in {
      setupSubmitMocks(Some(missingDateKIModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isKnowledgeIntensive" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit with a false KI Model to the IsKnowledgeIntensiveControlle when authenticated and enrolled" should {
    "redirect to the subsidiaries" in {
      setupSubmitMocks(Some(falseKIModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isKnowledgeIntensive" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit without a KI Model to the IsKnowledgeIntensiveController when authenticated and enrolled" should {
    "redirect to the date of incorporation" in {
      setupSubmitMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isKnowledgeIntensive" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the IsKnowledgeIntensiveController when authenticated and enrolled" should {
    "redirect to the subsidiaries" in {
      setupSubmitMocks(Some(updatedKIModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isKnowledgeIntensive" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the IsKnowledgeIntensiveController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isKnowledgeIntensive" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
