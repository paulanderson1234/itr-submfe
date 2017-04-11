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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.fileUpload.{EnvelopeFile, Metadata}
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.omg.CosNaming.NamingContextPackage.NotFound
import play.api.test.Helpers._

import scala.concurrent.Future

class SupportingDocumentsUploadControllerSpec extends BaseSpec {


  object TestController extends SupportingDocumentsUploadController {
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig = MockConfig
    override val fileUploadService = mockFileUploadService
  }

  "SupportingDocumentsUploadController" should {
    "use the correct auth connector" in {
      CheckDocumentsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      CheckDocumentsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      CheckDocumentsController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct registration service" in {
      CheckDocumentsController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to SupportingDocumentsUploadController when authenticated and enrolled" +
    "with UploadFeature Enabled" should {
    "return a 200" in {
      when(TestController.fileUploadService.getUploadFeatureEnabled).thenReturn(true)
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Sending a GET request to SupportingDocumentsUploadController when authenticated and enrolled" +
    "with UploadFeature disabled" should {
    "return a 404" in {
      when(TestController.fileUploadService.getUploadFeatureEnabled).thenReturn(false)
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe NOT_FOUND
        }
      )
    }
  }

  "Posting to the SupportingDocumentsUploadController when authenticated and enrolled" should {
    "redirect to attachments file upload page" in {
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit){
        result => status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(TestController.applicationConfig.attachmentFileUploadOutsideUrl)
      }
    }
  }

  "Sending a cancel request to SupportingDocumentsUploadController when authenticated and enrolled " should {
    "redirect to hub page" in {
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.cancel){
        result => status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ApplicationHubController.show().url)
      }
    }
  }
}
