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

package controllers.seis

import java.net.URLEncoder

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.ControllerSpec
import models.SupportingDocumentsUploadModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.FileUploadService

import scala.concurrent.Future

class SupportingDocumentsUploadControllerSpec extends ControllerSpec {

  object TestController extends SupportingDocumentsUploadController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override val s4lConnector = mockS4lConnector
    override val fileUploadService = mockFileUploadService
    override val attachmentsFrontEndUrl = MockConfig.attachmentFileUploadUrl(Constants.schemeTypeSeis.toLowerCase)
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val supportingDocumentsUploadDoUpload = SupportingDocumentsUploadModel("Yes")
  val supportingDocumentsUploadDontDoUpload = SupportingDocumentsUploadModel("No")

  def setupMocks(backLink: Option[String] = None, supportingDocumentsUploadModel: Option[SupportingDocumentsUploadModel] = None,
                 uploadFeatureEnabled: Boolean = true): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSupportingDocs))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[SupportingDocumentsUploadModel](Matchers.eq(KeystoreKeys.supportingDocumentsUpload))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(supportingDocumentsUploadModel))
    when(mockFileUploadService.getUploadFeatureEnabled).thenReturn(uploadFeatureEnabled)
  }

    "SupportingDocumentsUploadController" should {
      "use the correct keystore connector" in {
        SupportingDocumentsUploadController.s4lConnector shouldBe S4LConnector
      }
      "use the correct auth connector" in {
        SupportingDocumentsUploadController.authConnector shouldBe FrontendAuthConnector
      }
      "use the correct enrolment connector" in {
        SupportingDocumentsUploadController.enrolmentConnector shouldBe EnrolmentConnector
      }
      "use the correct upload service" in {
        SupportingDocumentsUploadController.fileUploadService shouldBe FileUploadService
      }
    }

    "Sending a GET request to SupportingDocumentsUploadController with upload feature enabled" should {
      "return a 200 OK" in {
        mockEnrolledRequest()
        setupMocks(Some(routes.ConfirmCorrespondAddressController.show().url), Some(supportingDocumentsUploadDoUpload), true)
        showWithSessionAndAuth(TestController.show)(
          result => status(result) shouldBe OK
        )
      }
    }

    "Sending a GET request to SupportingDocumentsUploadController with upload feature disabled" should {
      "return a 404 NOT_FOUND" in {
        mockEnrolledRequest()
        setupMocks(Some(routes.ConfirmCorrespondAddressController.show().url), None, false)
        showWithSessionAndAuth(TestController.show)(
          result => status(result) shouldBe NOT_FOUND
        )
      }
    }

    "Sending a Get request to the SupportingDocumentsUploadController when authenticated and enrolled with upload feature disabled" should {
      "redirect to the confirm correspondence address page if no back link is found" in {
        mockEnrolledRequest()
        setupMocks()
        showWithSessionAndAuth(TestController.show)(
          result => {
            status(result) shouldBe SEE_OTHER
          }
        )
      }
    }

    "Sending a Get request to the SupportingDocumentsUploadController when authenticated and enrolled with upload feature enabled" should {
      "redirect to the confirm correspondence address page if no SupportingDocumentsUploadModel is found" in {
        mockEnrolledRequest()
        setupMocks(Some(routes.ConfirmCorrespondAddressController.show().url))
        showWithSessionAndAuth(TestController.show)(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

    "Posting to the SupportingDocumentsUploadController when authenticated and enrolled and with upload feature enabled" should {
      "redirect to Check your answers page" in {
        mockEnrolledRequest()
        setupMocks()
        submitWithSessionAndAuth(TestController.submit, "doUpload" -> Constants.StandardRadioButtonYesValue){
          result => status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(TestController.attachmentsFrontEndUrl)
        }
      }
    }

    "Posting to the SupportingDocumentsUploadController when authenticated and enrolled and with upload feature disabled" should {
      "redirect to Check your answers page" in {
        mockEnrolledRequest()
        setupMocks()
        submitWithSessionAndAuth(TestController.submit, "doUpload" -> Constants.StandardRadioButtonNoValue){
          result => status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/seis/check-your-answers")
        }
      }
    }

  "Posting to the SupportingDocumentsUploadController when authenticated and enrolled with a form with errors" should {
    "redirect to itself when a backlink is found" in {
      mockEnrolledRequest()
      setupMocks(Some(routes.ConfirmCorrespondAddressController.show().url), Some(supportingDocumentsUploadDoUpload), true)
      submitWithSessionAndAuth(TestController.submit, "doUpload" -> "") {
        result => status(result) shouldBe BAD_REQUEST
      }
    }
  }

  "Posting to the SupportingDocumentsUploadController when authenticated and enrolled with a form with errors" should {
    "redirect to ProposedInvestment when no backlink is found" in {
      mockEnrolledRequest()
      setupMocks(None, Some(supportingDocumentsUploadDoUpload), true)
      submitWithSessionAndAuth(TestController.submit, "doUpload" -> "") {
        result => status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/seis/proposed-investment")
      }
    }
  }

  "Sending a request with no session to SupportingDocumentsUploadController" should {
    "return a 303" in {
      status(TestController.show(fakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(TestController.show(fakeRequest)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending an Unauthenticated request with a session to SupportingDocumentsUploadController" should {
    "return a 303" in {
      status(TestController.show(fakeRequestWithSession)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(TestController.show(fakeRequestWithSession)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending a timed-out request to SupportingDocumentsUploadController" should {

    "return a 303 in" in {
      status(TestController.show(timedOutFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to timeout page" in {
      redirectLocation(TestController.show(timedOutFakeRequest)) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
    }
  }

  "Sending a SupportingDocumentsUploadController when NOT enrolled" should {

    lazy val result = TestController.show(authorisedFakeRequest)

    "return a 303 in" in {
      mockNotEnrolledRequest()
      status(result) shouldBe SEE_OTHER
    }

    s"should redirect to Subscription Service" in {
      mockNotEnrolledRequest()
      redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
    }
  }

  "Sending a submission to the SupportingDocumentsUploadController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the SupportingDocumentsUploadController with no session" should {

    "redirect to the GG login page with no session" in {
      submitWithoutSession(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the SupportingDocumentsUploadController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the SupportingDocumentsUploadController when NOT enrolled" should {
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
