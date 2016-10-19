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

import auth._
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, KeystoreConnector, SubmissionConnector}
import controllers.helpers.FakeRequestHelper
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.specs2.mock.Mockito
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import java.net.URLEncoder

import auth.AuthEnrolledTestController.{INTERNAL_SERVER_ERROR => _, OK => _, SEE_OTHER => _, _}
import fixtures.SubmissionFixture
import models.submission.SubmissionResponse

import scala.concurrent.Future

class AcknowledgementControllerSpec extends UnitSpec  with Mockito with WithFakeApplication  with FakeRequestHelper with SubmissionFixture{

  val mockKeyStoreConnector = mock[KeystoreConnector]
  val mockHttp : WSHttp = mock[WSHttp]
  val mockSubmission = mock[SubmissionConnector]

  val contactValid = ContactDetailsModel("Frank","The Tank","01384 555678","email@gmail.com")
  val contactInvalid = ContactDetailsModel("Frank","The Tank","01384 555678","email@badrequest.com")
  val yourCompanyNeed = YourCompanyNeedModel("AA")
  val submissionRequestValid = SubmissionRequest(contactValid,yourCompanyNeed)
  val submissionRequestInvalid = SubmissionRequest(contactInvalid,yourCompanyNeed)
  val submissionResponse = SubmissionResponse("2014-12-17","FBUND09889765")


  class SetupPage() {
    val controller = new AcknowledgementController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
      val submissionConnector: SubmissionConnector = mockSubmission
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    def mockEnrolledRequest(): Unit = when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))
    def mockNotEnrolledRequest(): Unit = when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(None))

  }

  class SetupPageFull() {
    val controller = new AcknowledgementController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
      val submissionConnector: SubmissionConnector = mockSubmission
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    def mockEnrolledRequest(): Unit = when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))
    def mockNotEnrolledRequest(): Unit = when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(None))

    setUpMocks(mockKeyStoreConnector)
  }

  class SetupPageMinimum() {

    val controller = new AcknowledgementController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
      val submissionConnector: SubmissionConnector = mockSubmission
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    def mockEnrolledRequest(): Unit = when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))
    def mockNotEnrolledRequest(): Unit = when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(None))

    setUpMocksMinimumRequiredModels(mockKeyStoreConnector)
  }


  implicit val hc = HeaderCarrier()

  "AcknowledgementController" should {
    "use the correct keystore connector" in {
      AcknowledgementController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "AcknowledgementController" should {
    "use the correct auth connector" in {
      AcknowledgementController.authConnector shouldBe FrontendAuthConnector
    }
  }

  "AcknowledgementController" should {
    "use the correct submission connector" in {
      AcknowledgementController.submissionConnector shouldBe SubmissionConnector
    }
  }

  "AcknowledgementController" should {
    "use the correct enrolment connector" in {
      AcknowledgementController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 200 when a valid submission data is submitted" in new SetupPageFull{
      when(mockSubmission.submitAdvancedAssurance(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))

      mockEnrolledRequest()
      val result = controller.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }

    "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
      "return a 200 when a valid submission data is submitted with minimum expected data" in new SetupPageMinimum {
        when(mockSubmission.submitAdvancedAssurance(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
        mockEnrolledRequest()
        val result = controller.show.apply(authorisedFakeRequest)
        status(result) shouldBe OK
      }
    }

    "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
      "return a 303 redirect if mandatory KiProcessingModel is missing from keystore" in new SetupPage {

        setUpMocksTestMinimumRequiredModels(mockKeyStoreConnector,kiModel = None,
          Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
          Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress))

        when(mockSubmission.submitAdvancedAssurance(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
        mockEnrolledRequest()
        val result = controller.show.apply(authorisedFakeRequest)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.IntroductionController.show().url)
      }
    }

    "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
      "return a 303 redirect if mandatory NatureOfBusinessModel is missing from keystore" in new SetupPage {

        setUpMocksTestMinimumRequiredModels(mockKeyStoreConnector,Some(kiProcModelValid),
          natureBusiness = None, Some(contactValid), Some(proposedInvestmentValid),
          Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress))

        when(mockSubmission.submitAdvancedAssurance(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
        mockEnrolledRequest()
        val result = controller.show.apply(authorisedFakeRequest)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.IntroductionController.show().url)
      }
    }

    "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
      "return a 303 redirect if mandatory ContactDetailsModel is missing from keystore" in new SetupPage {

        setUpMocksTestMinimumRequiredModels(mockKeyStoreConnector,Some(kiProcModelValid),
          Some(natureOfBusinessValid), contactDetails = None, Some(proposedInvestmentValid),
          Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress))

        when(mockSubmission.submitAdvancedAssurance(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
        mockEnrolledRequest()
        val result = controller.show.apply(authorisedFakeRequest)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.IntroductionController.show().url)
      }
    }

    "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
      "return a 303 redirect if mandatory ProposedInvestmentModel is missing from keystore" in new SetupPage {

        setUpMocksTestMinimumRequiredModels(mockKeyStoreConnector,Some(kiProcModelValid),
          Some(natureOfBusinessValid), Some(contactValid), proposedInvestment = None,
          Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress))

        when(mockSubmission.submitAdvancedAssurance(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
        mockEnrolledRequest()
        val result = controller.show.apply(authorisedFakeRequest)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.IntroductionController.show().url)
      }
    }

    "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
      "return a 303 redirect if mandatory InvestmentGrowModel is missing from keystore" in new SetupPage {

        setUpMocksTestMinimumRequiredModels(mockKeyStoreConnector,Some(kiProcModelValid),
          Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
          investGrow = None, Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress))

        when(mockSubmission.submitAdvancedAssurance(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
        mockEnrolledRequest()
        val result = controller.show.apply(authorisedFakeRequest)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.IntroductionController.show().url)
      }
    }

    "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
      "return a 303 redirect if mandatory DateOfIncorporationModel is missing from keystore" in new SetupPage {

        setUpMocksTestMinimumRequiredModels(mockKeyStoreConnector,Some(kiProcModelValid),
          Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
          Some(investmentGrowValid), dateIncorp = None, Some(fullCorrespondenceAddress))

        when(mockSubmission.submitAdvancedAssurance(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
        mockEnrolledRequest()
        val result = controller.show.apply(authorisedFakeRequest)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.IntroductionController.show().url)
      }
    }

    "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
      "return a 303 redirect if mandatory AddressModel (contact address) is missing from keystore" in new SetupPage {

        setUpMocksTestMinimumRequiredModels(mockKeyStoreConnector,Some(kiProcModelValid),
          Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
          Some(investmentGrowValid),  Some(dateOfIncorporationValid), contactAddress = None)

        when(mockSubmission.submitAdvancedAssurance(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
        mockEnrolledRequest()
        val result = controller.show.apply(authorisedFakeRequest)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.IntroductionController.show().url)
      }
    }

    "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
      "return a 200 if KI is set to false" in new SetupPage {
        when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(kiProcModelValidAssertNo)))
        when(mockKeyStoreConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(natureOfBusinessValid)))
        when(mockKeyStoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(contactDetailsValid)))
        when(mockKeyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(proposedInvestmentValid)))
        when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(investmentGrowValid)))
        when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(dateOfIncorporationValid)))
        when(mockKeyStoreConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(fullCorrespondenceAddress)))

        when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeyStoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeyStoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeyStoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeyStoreConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(turnoverCostsValid)))

        when(mockSubmission.submitAdvancedAssurance(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
        mockEnrolledRequest()
        val result = controller.show.apply(authorisedFakeRequest)
        status(result) shouldBe OK

      }
    }

    "return a 5xx when an invalid email is submitted" in new SetupPageFull{

      when(mockSubmission.submitAdvancedAssurance(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      mockEnrolledRequest()
      val result = controller.show.apply(authorisedFakeRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Sending an Authenticated and NOT Enrolled GET request with a session to AcknowledgementController" should {
    "redirect to the TAVC Subscription Service" in new SetupPageFull{
      when(mockSubmission.submitAdvancedAssurance(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
      mockNotEnrolledRequest()
      val result = controller.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
    }
  }

  "Sending a request with no session to AcknowledgementController" should {
    "return a 302" in new SetupPageFull{
      val result = controller.show.apply(fakeRequest)
      status(result) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in new SetupPageFull{
      val result = controller.show.apply(fakeRequest)
      redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending an Unauthenticated request with a session to AcknowledgementController" should {
    "return a 302" in new SetupPageFull{
      val result = controller.show.apply(fakeRequestWithSession)
      status(result) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in new SetupPageFull{
      val result = controller.show.apply(fakeRequestWithSession)
      redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending a timed-out request to AcknowledgementController" should {

    "return a 303 in" in new SetupPageFull{
      val result = controller.show.apply(timedOutFakeRequest)
      status(result) shouldBe SEE_OTHER
    }

    s"should redirect to timeout page" in new SetupPage {
      val result = controller.show.apply(timedOutFakeRequest)
      redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
    }
  }
}
