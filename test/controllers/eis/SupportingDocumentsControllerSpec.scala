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
<<<<<<< HEAD:test/controllers/eis/SupportingDocumentsControllerSpec.scala
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.ControllerSpec
=======
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import helpers.BaseSpec
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/SupportingDocumentsControllerSpec.scala
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
<<<<<<< HEAD:test/controllers/eis/SupportingDocumentsControllerSpec.scala
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/confirm-correspondence-address")
=======
          redirectLocation(result) shouldBe Some(routes.ConfirmCorrespondAddressController.show().url)
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/SupportingDocumentsControllerSpec.scala
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
<<<<<<< HEAD:test/controllers/eis/SupportingDocumentsControllerSpec.scala
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/supporting-documents-upload")
=======
          redirectLocation(result) shouldBe Some(routes.SupportingDocumentsUploadController.show().url)
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/SupportingDocumentsControllerSpec.scala
      }
    }
  }

  "Posting to the SupportingDocumentsController when authenticated and enrolled" should {
    "redirect to Check your answers page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit){
        result => status(result) shouldBe SEE_OTHER
<<<<<<< HEAD:test/controllers/eis/SupportingDocumentsControllerSpec.scala
        redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/check-your-answers")
=======
        redirectLocation(result) shouldBe Some(routes.CheckAnswersController.show().url)
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/SupportingDocumentsControllerSpec.scala
      }
    }
  }

<<<<<<< HEAD:test/controllers/eis/SupportingDocumentsControllerSpec.scala
  "Sending a request with no session to SupportingDocumentsController" should {
    "return a 303" in {
      status(TestController.show(fakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(TestController.show(fakeRequest)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending an Unauthenticated request with a session to SupportingDocumentsController" should {
    "return a 303" in {
      status(TestController.show(fakeRequestWithSession)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(TestController.show(fakeRequestWithSession)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending a timed-out request to SupportingDocumentsController" should {

    "return a 303 in" in {
      status(TestController.show(timedOutFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to timeout page" in {
      redirectLocation(TestController.show(timedOutFakeRequest)) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
    }
  }

  "Sending a SupportingDocumentsController when NOT enrolled" should {

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

  "Sending a submission to the SupportingDocumentsController when not authenticated" should {

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

  "Sending a submission to the SupportingDocumentsController with no session" should {

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

  "Sending a submission to the SupportingDocumentsController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the SupportingDocumentsController when NOT enrolled" should {
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
=======
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/SupportingDocumentsControllerSpec.scala
}
