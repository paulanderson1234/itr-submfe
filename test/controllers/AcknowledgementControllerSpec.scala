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

import scala.concurrent.Future

class AcknowledgementControllerSpec extends UnitSpec  with Mockito with WithFakeApplication  with FakeRequestHelper{

  val mockKeyStoreConnector = mock[KeystoreConnector]
  val mockHttp : WSHttp = mock[WSHttp]
  val mockSubmission = mock[SubmissionConnector]

  val contactValid = ContactDetailsModel("Frank","The Tank","01384 555678","email@gmail.com")
  val contactInvalid = ContactDetailsModel("Frank","The Tank","01384 555678","email@badrequest.com")
  val yourCompanyNeed = YourCompanyNeedModel("AA")
  val submissionRequestValid = SubmissionRequest(contactValid,yourCompanyNeed)
  val submissionRequestInvalid = SubmissionRequest(contactInvalid,yourCompanyNeed)
  val submissionResponse = SubmissionResponse(true,"FBUND09889765", "Submission Request Successful")

  class SetupPage {

    val controller = new AcknowledgementController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
      val submissionConnector: SubmissionConnector = mockSubmission
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    def mockEnrolledRequest: Unit = when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

    def mockNotEnrolledRequest: Unit = when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(None))
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
    "return a 200 when a valid email address submitted" in new SetupPage{
      when(mockKeyStoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(contactValid)))
      when(mockKeyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(yourCompanyNeed)))
      when(mockSubmission.submitAdvancedAssurance(Matchers.eq(submissionRequestValid))(Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
      mockEnrolledRequest
      val result = controller.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }

    "return a 5xx when an invalid email is submitted" in new SetupPage{
      when(mockKeyStoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(contactInvalid)))
      when(mockKeyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(yourCompanyNeed)))
      when(mockSubmission.submitAdvancedAssurance(Matchers.eq(submissionRequestInvalid))(Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      mockEnrolledRequest
      val result = controller.show.apply(authorisedFakeRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Sending an Authenticated and NOT Enrolled GET request with a session to AcknowledgementController" should {
    "redirect to the TAVC Subscription Service" in new SetupPage{
      when(mockKeyStoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(contactValid)))
      when(mockKeyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(yourCompanyNeed)))
      when(mockSubmission.submitAdvancedAssurance(Matchers.eq(submissionRequestValid))(Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
      mockNotEnrolledRequest
      val result = controller.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
    }
  }


  "Sending a request with no session to AcknowledgementController" should {
    "return a 302" in new SetupPage{
      val result = controller.show.apply(fakeRequest)
      status(result) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in new SetupPage{
      val result = controller.show.apply(fakeRequest)
      redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending an Unauthenticated request with a session to AcknowledgementController" should {
    "return a 302" in new SetupPage{
      val result = controller.show.apply(fakeRequestWithSession)
      status(result) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in new SetupPage{
      val result = controller.show.apply(fakeRequestWithSession)
      redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending a timed-out request to AcknowledgementController" should {

    "return a 303 in" in new SetupPage{
      val result = controller.show.apply(timedOutFakeRequest)
      status(result) shouldBe SEE_OTHER
    }

    s"should redirect to timeout page" in new SetupPage {
      val result = controller.show.apply(timedOutFakeRequest)
      redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
    }
  }




}
