/*
 * Copyright 2016 HM Revenue & Customs
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

import java.net.URLEncoder

import auth.{MockAuthConnector, MockConfig}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.EnrolmentConnector
import controllers.helpers.ControllerSpec
import org.mockito.Mockito._
import org.mockito.Matchers
import play.api.test.Helpers._
import services.FileUploadService
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.Future

class FileDeleteControllerSpec extends ControllerSpec {

  val envelopeID = "00000000-0000-0000-0000-000000000000"
  val fileName = "test.pdf"
  val fileID = "1"

  object TestController extends FileDeleteController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val fileUploadService = mockFileUploadService
  }

  "FileDeleteController" should {
    "use the correct auth connector" in {
      FileDeleteController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      FileDeleteController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct file upload service" in {
      FileDeleteController.fileUploadService shouldBe FileUploadService
    }
  }

  "Sending a GET request to FileDeleteController when authenticated and enrolled" should {
    "return a 200" in {
      when(mockFileUploadService.getEnvelopeID(Matchers.eq(false))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(envelopeID))
      when(mockFileUploadService.getEnvelopeFiles(Matchers.eq(envelopeID))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(files))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show(fileID))(
        result => status(result) shouldBe OK
      )
    }

  }

  "Sending a GET request to FileDeleteController when authenticated and NOT enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show(fileID))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to FileDeleteController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(TestController.show(fileID))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to FileDeleteController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(TestController.show(fileID))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to FileDeleteController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show(fileID))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }
  
  "Posting to the FileDeleteController when authenticated and enrolled" should {

    "redirect to 'FileUpload' page when form contains a fileID and deleteFile returns OK" in {
      mockEnrolledRequest()
      val formInput = "file-id" -> fileID
      when(mockFileUploadService.deleteFile(Matchers.eq(fileID))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.FileUploadController.show().url)
        }
      )
    }

    "return InternalServerError page when form contains a fileID and deleteFile returns non-OK" in {
      mockEnrolledRequest()
      val formInput = "file-id" -> fileID
      when(mockFileUploadService.deleteFile(Matchers.eq(fileID))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }

    "return InternalServerError page when form does not contain a fileID" in {
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }

  "Posting to the FileDeleteController when not authenticated" should {

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

  "Sending a submission to the FileDeleteController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the FileDeleteController when not enrolled" should {
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
