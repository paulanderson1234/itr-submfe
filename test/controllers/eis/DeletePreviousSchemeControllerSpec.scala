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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HttpResponse

class DeletePreviousSchemeControllerSpec extends BaseSpec{

  object TestDeletePreviousSchemeController extends DeletePreviousSchemeController{
    override lazy val s4lConnector = S4LConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig= MockConfig
    override protected def authConnector = MockAuthConnector
  }


  "DeletePreviousSchemeControllerSpec" should {
    "use the correct keystore connector" in {
      DeletePreviousSchemeController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrollment connector" in {
      DeletePreviousSchemeController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "use the correct config" in {
      DeletePreviousSchemeController.applicationConfig shouldBe FrontendAppConfig
    }

    "use the correct auth connector" in {
      DeletePreviousSchemeController.authConnector shouldBe FrontendAuthConnector
    }
  }

  def setupShowMocks(){
    when(DeletePreviousSchemeController.fileUploadService.getEnvelopeID(Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(envelopeId)
    when(DeletePreviousSchemeController.fileUploadService.getEnvelopeFiles(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(maxFiles)
  }
  def setupSubmitMocks(httpResponse: HttpResponse){
    when(DeletePreviousSchemeController.fileUploadService.deleteFile(Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).
      thenReturn(httpResponse)

  }

  "Issuing a GET request to the TestDeletePreviousSchemeController when authenticated and enrolled" should {
    "return a 200 Ok" in {
      mockEnrolledRequest()
      setupShowMocks()
      showWithSessionAndAuth(TestDeletePreviousSchemeController.show(fileId))(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Issuing a GET request to the TestDeletePreviousSchemeController when authenticated and not enrolled" should {
    "redirect to the subscription service" in {
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestDeletePreviousSchemeController.show(schemeId))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(TestDeletePreviousSchemeController.applicationConfig.subscriptionUrl)
        }
      )
    }
  }

  "Issuing a GET request to the TestDeletePreviousSchemeController neither authenticated or enrolled" should {
    "redirect to the gg login" in {
      mockNotEnrolledRequest()
      showWithSessionWithoutAuth(TestDeletePreviousSchemeController.show(schemeId))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${TestDeletePreviousSchemeController.applicationConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(TestDeletePreviousSchemeController.applicationConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-attachments-frontend&accountType=organisation")
        }
      )
    }
  }

  "Issuing a GET request to the TestDeletePreviousSchemeController with no session" should {
    "redirect to the gg login" in {
      mockNotEnrolledRequest()
      showWithoutSession(TestDeletePreviousSchemeController.show(schemeId))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${TestDeletePreviousSchemeController.applicationConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(TestDeletePreviousSchemeController.applicationConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-delete-previous-scheme&accountType=organisation")
        }
      )
    }
  }

  "Issuing a Timed out GET request to the FileDeleteController" should {
    "redirect to the Timeout page" in {
      mockNotEnrolledRequest()
      showWithTimeout(TestFileDeleteController.show(fileId))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }




  "Issuing a POST request to the FileDeleteController when authenticated and enrolled" should {
    "redirect to the file upload page when the file is successfully deleted" in {
      mockEnrolledRequest()
      setupSubmitMocks(HttpResponse(OK))
      val formInput = "file-id" -> fileId
      submitWithSessionAndAuth(TestFileDeleteController.submit(),formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.FileUploadController.show().url)
        }
      )
    }
  }

  "Issuing a POST request to the FileDeleteController when authenticated and enrolled" should {
    "return an INTERNAL_SERVER_ERROR if the file cannot be deleted" in {
      mockEnrolledRequest()
      setupSubmitMocks(HttpResponse(INTERNAL_SERVER_ERROR))
      val formInput = "file-id" -> fileId
      submitWithSessionAndAuth(TestFileDeleteController.submit(), formInput)(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }

  "Issuing a POST request to the FileDeleteController when authenticated and enrolled" should {
    "return an INTERNAL_SERVER_ERROR when a form with errors is posted" in {
      mockEnrolledRequest()
      val formInput = "file-id" -> ""
      submitWithSessionAndAuth(TestFileDeleteController.submit(),formInput)(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }


  "Issuing a POST request to the FileDeleteController when authenticated and not enrolled" should {
    "redirect to the subscription service" in {
      mockNotEnrolledRequest()
      submitWithSessionAndAuth(TestFileDeleteController.submit())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(TestFileDeleteController.applicationConfig.subscriptionUrl)
        }
      )
    }
  }

  "Issuing a POST request to the FileDeleteController neither authenticated or enrolled" should {
    "redirect to the gg login screen" in {
      mockNotEnrolledRequest()
      submitWithSessionWithoutAuth(TestFileDeleteController.submit())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${TestFileDeleteController.applicationConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(TestFileDeleteController.applicationConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-attachments-frontend&accountType=organisation")
        }
      )
    }
  }

  "Issuing a POST request to the FileDeleteController with no session" should {
    "redirect to the gg login screen" in {
      mockNotEnrolledRequest()
      submitWithoutSession(TestFileDeleteController.submit())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${TestFileDeleteController.applicationConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(TestFileDeleteController.applicationConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-attachments-frontend&accountType=organisation")
        }
      )
    }
  }

  "Issuing a Timed out POST request to the FileDeleteController" should {
    "redirect to the Timeout Page" in {
      mockNotEnrolledRequest()
      submitWithTimeout(TestFileDeleteController.submit())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }
}
