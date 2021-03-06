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

import auth.AuthEnrolledTestController.{ACCEPTED => _, INTERNAL_SERVER_ERROR => _, NO_CONTENT => _, OK => _, SEE_OTHER => _, _}
import auth._
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.feedback
import controllers.helpers.BaseSpec
import models.submission.{SchemeTypesModel, SubmissionResponse}
import models.{ContactDetailsModel, SubmissionRequest, TradeStartDateModel, YourCompanyNeedModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.FileUploadService

import scala.concurrent.Future
import uk.gov.hmrc.http.HttpResponse

class AttachmentsAcknowledgementControllerSpec extends BaseSpec {

  val contactValid = ContactDetailsModel("first", "last", Some("07000 111222"), None, "test@test.com")
  val contactInvalid = ContactDetailsModel("first", "last", Some("07000 111222"), None, "test@badrequest.com")
  val yourCompanyNeed = YourCompanyNeedModel("AA")
  val submissionRequestValid = SubmissionRequest(contactValid, yourCompanyNeed)
  val submissionRequestInvalid = SubmissionRequest(contactInvalid, yourCompanyNeed)
  val submissionResponse = SubmissionResponse("2014-12-17", "FBUND09889765")

  object TestController extends AttachmentsAcknowledgementController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val registrationDetailsService = mockRegistrationDetailsService
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val fileUploadService = mockFileUploadService
    override lazy val emailConfirmationService = mockEmailConfirmationService
  }

  class SetupPageFull() {
    setUpMocks(mockS4lConnector)
    setUpMocksRegistrationService(mockRegistrationDetailsService)
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(tradeStartDateModelYes))
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
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(tradeStartDateModelYes))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(schemeTypesSEIS))

    when(mockFileUploadService.closeEnvelope(Matchers.any(), Matchers.any())(Matchers.any(),Matchers.any(), Matchers.any())).
      thenReturn(Future(HttpResponse(OK)))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeId))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(envelopeId))
    when(mockEmailConfirmationService.sendEmailConfirmation(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).
      thenReturn(HttpResponse(ACCEPTED))
    when(mockS4lConnector.clearCache()(Matchers.any(),Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
  }

  "AttachmentsAcknowledgementController" should {
    "use the correct keystore connector" in {
      AttachmentsAcknowledgementController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      AttachmentsAcknowledgementController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct submission connector" in {
      AttachmentsAcknowledgementController.submissionConnector shouldBe SubmissionConnector
    }
    "use the correct enrolment connector" in {
      AttachmentsAcknowledgementController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct file upload service" in {
      AttachmentsAcknowledgementController.fileUploadService shouldBe FileUploadService
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AttachmentsAcknowledgementController for SEIS" should {
    "return a 200, close the file upload envelope, send an email " +
      "delete the current application when a valid submission data is submitted" in new SetupPageFull {
      when(mockFileUploadService.closeEnvelope(Matchers.any(), Matchers.any())(Matchers.any(),Matchers.any(), Matchers.any())).thenReturn(Future(HttpResponse(OK)))
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeId))
        (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(envelopeId))
      when(mockEmailConfirmationService.sendEmailConfirmation(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).
        thenReturn(HttpResponse(ACCEPTED))
      when(mockS4lConnector.clearCache()(Matchers.any(),Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }
  "Sending an Authenticated and Enrolled GET request with a session to AttachmentsAcknowledgementController for SEIS" should {
    "return a 200 and delete the current application and send an email when a valid submission data is submitted with minimum data" in new SetupPageMinimum {
      when(mockEmailConfirmationService.sendEmailConfirmation(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).
        thenReturn(HttpResponse(ACCEPTED))
      when(mockS4lConnector.clearCache()(Matchers.any(),Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AttachmentsAcknowledgementController for SEIS" should {
    "return a 303 redirect if mandatory NatureOfBusinessModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        natureBusiness = None, Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), returnRegistrationDetails = true)
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AttachmentsAcknowledgementController for SEIS" should {
    "return a 303 redirect if mandatory ContactDetailsModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), contactDetails = None, Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), returnRegistrationDetails = true)
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AttachmentsAcknowledgementController for SEIS" should {
    "return a 303 redirect if mandatory ProposedInvestmentModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), proposedInvestment = None,
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), returnRegistrationDetails = true)
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AttachmentsAcknowledgementController for SEIS" should {
    "return a 303 redirect if mandatory DateOfIncorporationModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), dateIncorp = None, Some(fullCorrespondenceAddress), returnRegistrationDetails = true)
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AttachmentsAcknowledgementController for SEIS" should {
    "return a 303 redirect if mandatory AddressModel (contact address) is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), contactAddress = None, returnRegistrationDetails = true)
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AttachmentsAcknowledgementController for SEIS" should {
    "return a 303 redirect if mandatory registrationDetailsModel is from registration details service" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), returnRegistrationDetails = false)
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AttachmentsAcknowledgementController for SEIS" should {
    "return a 5xx when an invalid email is submitted" in new SetupPageFull {
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(schemeTypesEIS))
      when(mockSubmissionConnector.submitAdvancedAssurance(Matchers.any(), Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Sending a POST request to the Attachments Acknowledgement controller for SEIS when authenticated and enrolled" should {
    "redirect to the feedback page" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(config.FrontendAppConfig.feedbackUrl)
        }
      )
    }
  }
}