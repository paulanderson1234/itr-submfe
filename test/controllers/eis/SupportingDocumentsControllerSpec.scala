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
import controllers.helpers.BaseSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.FileUploadService

import scala.concurrent.Future

class SupportingDocumentsControllerSpec extends BaseSpec {

  object TestController extends SupportingDocumentsController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override val fileUploadService = mockFileUploadService
    override val attachmentsFrontEndUrl = MockConfig.attachmentFileUploadUrl("eis")
    override lazy val enrolmentConnector = mockEnrolmentConnector

  }

  def setupMocks(backLink: Option[String] = None, uploadFeatureEnabled: Boolean = false): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSupportingDocs))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backLink))
      when(mockFileUploadService.getUploadFeatureEnabled).thenReturn(uploadFeatureEnabled)
  }

  "SupportingDocumentsController" should {
    "use the correct keystore connector" in {
      SupportingDocumentsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      SupportingDocumentsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      SupportingDocumentsController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct upload service" in {
      SupportingDocumentsController.fileUploadService shouldBe FileUploadService
    }
  }

  "Sending a GET request to SupportingDocumentsController with upload feature disabled" should {
    "return a 200 OK" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupMocks(Some(routes.ConfirmCorrespondAddressController.show().url), false)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "sending a Get requests to the SupportingDocumentsController when authenticated and enrolled with upload feature disabled" should {
    "redirect to the confirm correspondence address page if no saved back link was found" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupMocks()
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ConfirmCorrespondAddressController.show().url)
        }
      )
    }
  }


  "Sending a GET request to SupportingDocumentsController with upload feature enabled" should {
    "redirect to the upload file supporting documents page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupMocks(Some(routes.ConfirmCorrespondAddressController.show().url), true)
      showWithSessionAndAuth(TestController.show) {
          result => status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SupportingDocumentsUploadController.show().url)
      }
    }
  }

  "Posting to the SupportingDocumentsController when authenticated and enrolled" should {
    "redirect to Check your answers page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit){
        result => status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CheckAnswersController.show().url)
      }
    }
  }
}
