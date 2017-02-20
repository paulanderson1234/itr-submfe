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

import auth._
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.FileUploadService
import uk.gov.hmrc.play.http.HttpResponse
import auth.AuthEnrolledTestController.{INTERNAL_SERVER_ERROR => _, NO_CONTENT => _, OK => _, SEE_OTHER => _, _}
import models.{ContactDetailsModel, SubmissionRequest, YourCompanyNeedModel}
import models.submission.{SchemeTypesModel, SubmissionResponse}

import scala.concurrent.Future

class AcknowledgementControllerSpec extends BaseSpec {

  val contactValid = ContactDetailsModel("first", "last", Some("07000 111222"), None, "test@test.com")
  val contactInvalid = ContactDetailsModel("first", "last", Some("07000 111222"), None, "test@badrequest.com")
  val yourCompanyNeed = YourCompanyNeedModel("AA")
  val submissionRequestValid = SubmissionRequest(contactValid, yourCompanyNeed)
  val submissionRequestInvalid = SubmissionRequest(contactInvalid, yourCompanyNeed)
  val submissionResponse = SubmissionResponse("2014-12-17", "FBUND09889765")


  object TestController extends AcknowledgementController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override val registrationDetailsService = mockRegistrationDetailsService
    override lazy val s4lConnector = mockS4lConnector
    override val submissionConnector = mockSubmissionConnector
    override lazy val fileUploadService = mockFileUploadService
  }

  class SetupPageFull() {
    setUpMocks(mockS4lConnector)
    setUpMocksRegistrationService(mockRegistrationDetailsService)
  }

  class SetupPageMinimum() {

    when(mockSubmissionConnector.submitAdvancedAssurance(Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
    setUpMocksMinimumRequiredModels(mockS4lConnector)
    setUpMocksRegistrationService(mockRegistrationDetailsService)
  }

  def setupMocks(): Unit = {
    when(mockSubmissionConnector.submitAdvancedAssurance(Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(schemeTypesEIS))
  }

  "AcknowledgementController" should {
    "use the correct keystore connector" in {
      AcknowledgementController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      AcknowledgementController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct submission connector" in {
      AcknowledgementController.submissionConnector shouldBe SubmissionConnector
    }
    "use the correct enrolment connector" in {
      AcknowledgementController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct file upload service" in {
      AcknowledgementController.fileUploadService shouldBe FileUploadService
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 200 and delete the current application when a valid submission data is submitted" in new SetupPageFull {
      when(mockFileUploadService.getUploadFeatureEnabled).thenReturn(false)
      when(mockS4lConnector.clearCache()(Matchers.any(),Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 200, close the file upload envelope and " +
      "delete the current application when a valid submission data is submitted with the file upload flag enabled" in new SetupPageFull {
      when(mockFileUploadService.getUploadFeatureEnabled).thenReturn(true)
      when(mockFileUploadService.closeEnvelope(Matchers.any(), Matchers.any())(Matchers.any(),Matchers.any(), Matchers.any())).thenReturn(Future(HttpResponse(OK)))
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeId))
        (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(envelopeId))
      when(mockS4lConnector.clearCache()(Matchers.any(),Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 200 and delete the current application when a valid submission data is submitted with minimum expected data" in new SetupPageMinimum {
      when(mockFileUploadService.getUploadFeatureEnabled).thenReturn(false)
      when(mockS4lConnector.clearCache()(Matchers.any(),Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory KiProcessingModel is missing from keystore" in {
      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, kiModel = None,
        Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), true)
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory NatureOfBusinessModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        natureBusiness = None, Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), true)
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory ContactDetailsModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), contactDetails = None, Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), true)
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory ProposedInvestmentModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), proposedInvestment = None,
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), true)
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory InvestmentGrowModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
        investGrow = None, Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), true)
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory DateOfIncorporationModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), dateIncorp = None, Some(fullCorrespondenceAddress), true)
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory AddressModel (contact address) is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), contactAddress = None, true)
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory registrationDetailsModel is from registration details service" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), false)
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 200 if KI is set to false" in {
      when(mockS4lConnector.clearCache()(Matchers.any(),Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValidAssertNo),
        Some(natureOfBusinessValid), Some(contactDetailsValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), true)
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 5xx when an invalid email is submitted" in new SetupPageFull {
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(schemeTypesEIS))
      when(mockSubmissionConnector.submitAdvancedAssurance(Matchers.any(), Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Sending a POST request to the Acknowledgement controller when authenticated and enrolled" should {
    "redirect to the feedback page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.feedback.routes.FeedbackController.show().url)
        }
      )
    }
  }
}
