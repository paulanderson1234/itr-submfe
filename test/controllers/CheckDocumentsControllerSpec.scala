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
import org.mockito.Mockito._
import play.api.test.Helpers.{redirectLocation, _}
import services.FileUploadService

import scala.concurrent.Future

class CheckDocumentsControllerSpec extends BaseSpec {


  object TestController extends CheckDocumentsController {
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig = MockConfig
    override val fileUploadService = mockFileUploadService
  }

  "CheckDocumentsController" should {
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
    "use the correct service service" in {
      CheckDocumentsController.fileUploadService shouldBe FileUploadService
    }
  }

  def setupMocks(): Unit = {

    val files = Seq(EnvelopeFile("1","PROCESSING","test.pdf","application/pdf","2016-03-31T12:33:45Z",Metadata(None),"test.url"))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeId))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(envelopeId))
    when(mockFileUploadService.getEnvelopeFiles(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(files))
  }

  "Sending a GET request to CheckDocumentsController when authenticated and enrolled" should {

    "return a 200 when a valid envelopeId is received" in {
      setupMocks()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show(envelopeId.get))(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Posting to the CheckDocumentsController when authenticated and enrolled" should {
    "redirect to check documents acknowledgement page" in {
      setupMocks()
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit){
        result => status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.FileUploadAcknowledgementController.show().url)
      }
    }
  }

  "Sending a GET request to CheckDocumentsController when authenticated and enrolled to change the uploaded documents" should {
    "redirect to attachments file upload page" in {
      setupMocks()
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.redirectAttachments){
        result => status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(TestController.applicationConfig.attachmentFileUploadOutsideUrl)
      }
    }
  }
}
