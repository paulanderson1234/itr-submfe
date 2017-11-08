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

package auth

import java.net.URLEncoder

import common.KeystoreKeys
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import controllers.throttlingGuidance.routes
import models.submission.SchemeTypesModel
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import play.api.http.Status
import uk.gov.hmrc.play.frontend.auth.AuthenticationProviderIds
import play.api.test.Helpers._
import org.mockito.Matchers
import org.mockito.Mockito._

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier


class TAVCAuthEnrolledSpec extends BaseSpec {

  "Government Gateway Provider" should {
    "have an account type additional parameter set to organisation" in {
      val ggw = new GovernmentGatewayProvider(MockConfig.introductionUrl, MockConfig.ggSignInUrl)
      ggw.additionalLoginParameters("accountType") shouldEqual List("organisation")
    }
  }

  "Government Gateway Provider" should {
    "have a login url set from its second constructor parameter" in {
      val ggw = new GovernmentGatewayProvider(MockConfig.introductionUrl, MockConfig.ggSignInUrl)
      ggw.loginURL shouldEqual MockConfig.ggSignInUrl
    }
  }

  "Government Gateway Provider" should {
    "have a continueURL constructed from its first constructor parameter" in {
      val ggw = new GovernmentGatewayProvider(MockConfig.introductionUrl, MockConfig.ggSignInUrl)
      ggw.continueURL shouldEqual MockConfig.introductionUrl
    }
  }

  "Government Gateway Provider" should {
    "handle a session timeout with a redirect" in {
      implicit val fakeRequest = FakeRequest()
      val ggw = new GovernmentGatewayProvider(MockConfig.introductionUrl, MockConfig.ggSignInUrl)
      val timeoutHandler = ggw.handleSessionTimeout(fakeRequest)
      status(timeoutHandler) shouldBe SEE_OTHER
      redirectLocation(timeoutHandler) shouldBe Some("/investment-tax-relief/session-timeout")
    }
  }

  "Extract previously logged in time of logged in user" should {
    s"return ${ggUser.previouslyLoggedInAt.get}" in {
      val user = TAVCUser(ggUser.allowedAuthContext,internalId)
      user.previouslyLoggedInAt shouldBe ggUser.previouslyLoggedInAt
    }
  }

  "Calling authenticated async action with no login session with no tokenId" should {
    "result in a redirect to login" in {

      val result = AuthEnrolledTestController.authorisedAsyncAction(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      //redirectLocation(result) shouldBe Some(s"/gg/sign-in?continue=${URLEncoder.encode(MockConfig.introductionUrl)}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
      redirectLocation(result) shouldBe Some(s"/gg/sign-in?continue=${URLEncoder.encode(MockConfig.introductionUrl+"?tokenId=")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Calling authenticated async action with no login session and a tokenId" should {
    "result in a redirect to login" in {
      val result = AuthEnrolledTestController.authorisedAsyncTokenAction(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      //redirectLocation(result) shouldBe Some(s"/gg/sign-in?continue=${URLEncoder.encode(MockConfig.introductionUrl+"?tokenId=Some(123456789)")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
      redirectLocation(result) shouldBe Some(s"/gg/sign-in?continue=${URLEncoder.encode(MockConfig.introductionUrl+"?tokenId=123456789")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")

    }
  }

  "Calling authenticated async action with a default GG login session with no TAVC enrolment" should {
    "result in a redirect to subscription if there is a valid throttle token" in {
      implicit val hc = HeaderCarrier()
      when(AuthEnrolledTestController.s4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(AuthEnrolledTestController.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(None))
      when(AuthEnrolledTestController.enrolmentConnector.validateToken(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(true))
      val result = AuthEnrolledTestController.authorisedAsyncAction(authenticatedFakeRequest(AuthenticationProviderIds.GovernmentGatewayId))
      redirectLocation(result) shouldBe Some("/investment-tax-relief-subscription/?tokenId=")
    }
  }

  "Calling authenticated async action with a default GG login session with no TAVC enrolment  and no tokenId passed" should {
    "result in a redirect to subscription  if there is not a vaild throttle token" in {
      implicit val hc = HeaderCarrier()
      when(AuthEnrolledTestController.s4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(AuthEnrolledTestController.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(None))
      when(AuthEnrolledTestController.enrolmentConnector.validateToken(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(false))
      val result = AuthEnrolledTestController.authorisedAsyncAction(authenticatedFakeRequest(AuthenticationProviderIds.GovernmentGatewayId))
      redirectLocation(result) shouldBe Some(routes.OurServiceChangeController.show().url)
    }
  }

  "Calling authenticated async action with a default GG login session with no TAVC enrolment with a tokenId that has expired" should {
    "result in a redirect to subscription  if there is not a vaild throttle token" in {
      implicit val hc = HeaderCarrier()
      when(AuthEnrolledTestController.s4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(AuthEnrolledTestController.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(None))
      when(AuthEnrolledTestController.enrolmentConnector.validateToken(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(false))
      val result = AuthEnrolledTestController.authorisedAsyncTokenAction(authenticatedFakeRequest(AuthenticationProviderIds.GovernmentGatewayId))
      redirectLocation(result) shouldBe Some(routes.OurServiceChangeController.show().url)
    }
  }

  "Calling authenticated async action with a GG login session with a HMRC-TAVC-ORG enrolment" should {
    "result in a status OK" in {
      implicit val hc = HeaderCarrier()
      val enrolledUser = Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated")
      when(AuthEnrolledTestController.s4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(AuthEnrolledTestController.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Option(enrolledUser)))

      // should not need to mock validate token as already enrolled

      val result = AuthEnrolledTestController.authorisedAsyncAction(authenticatedFakeRequest(AuthenticationProviderIds.GovernmentGatewayId))
      status(result) shouldBe Status.OK
    }
  }

  "Calling authenticated async action with a GG login session with a HMRC-TAVC-ORG enrolment passing aokenId" should {
  "result in a status OK" in {
    implicit val hc = HeaderCarrier()
    val enrolledUser = Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated")
    when(AuthEnrolledTestController.s4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(AuthEnrolledTestController.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(enrolledUser)))
    // should not need to mock validate token as already enrolled

    val result = AuthEnrolledTestController.authorisedAsyncTokenAction(authenticatedFakeRequest(AuthenticationProviderIds.GovernmentGatewayId))
    status(result) shouldBe Status.OK
  }
}


  "Calling authenticated getTavCReferenceNumber when it is not found on the enrolement" should {
    "return an empty TavcReference" in {
      implicit val hc = HeaderCarrier()
      when(AuthEnrolledTestController.enrolmentConnector.getTavcReferenceNumber(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(""))
      val result = AuthEnrolledTestController.getTavCReferenceNumber()(hc)
      await(result) shouldBe ""
    }
  }

  "Calling authenticated getTavCReferenceNumber when it is exists on the enrolement" should {
    "return the TavcReference" in {
      implicit val hc = HeaderCarrier()

      when(AuthEnrolledTestController.enrolmentConnector.validateToken(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(true))

      when(AuthEnrolledTestController.enrolmentConnector.getTavcReferenceNumber(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful("XATAVC000123456"))

      val result = AuthEnrolledTestController.getTavCReferenceNumber()(hc)
      await(result) shouldBe "XATAVC000123456"
    }
  }

  "Calling authenticated async action and selected schemes are valid for the controller" should {
    "result in a status OK" in {
      implicit val hc = HeaderCarrier()
      object TestController extends AuthEnrolledTestController with MockitoSugar {
        override lazy val applicationConfig = mockConfig
        override lazy val authConnector = mockAuthConnector
        override lazy val enrolmentConnector = mock[EnrolmentConnector]
        override lazy val s4lConnector = mock[S4LConnector]
        override lazy val acceptedFlows = Seq(Seq(EIS))
      }
      val enrolledUser = Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated")
      when(TestController.s4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSchemeTypesModel))

      when(AuthEnrolledTestController.enrolmentConnector.validateToken(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(true))

      when(TestController.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Option(enrolledUser)))
      val result = TestController.authorisedAsyncAction(authenticatedFakeRequest(AuthenticationProviderIds.GovernmentGatewayId))
      status(result) shouldBe Status.OK
    }
  }

  "Calling authenticated async action and no schemeTypesModel is returned and controller accepts all scheme types" should {
    "result in status OK" in {
      implicit val hc = HeaderCarrier()
      val enrolledUser = Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated")
      when(AuthEnrolledTestController.s4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

      when(AuthEnrolledTestController.enrolmentConnector.validateToken(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(true))

      when(AuthEnrolledTestController.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Option(enrolledUser)))
      val result = AuthEnrolledTestController.authorisedAsyncAction(authenticatedFakeRequest(AuthenticationProviderIds.GovernmentGatewayId))
      status(result) shouldBe Status.OK
    }
  }

  "Calling authenticated async action and no schemeTypesModel is returned and controller only accepts specific schemes" should {
    "redirect to the application hub page" in {
      implicit val hc = HeaderCarrier()
      object TestController extends AuthEnrolledTestController with MockitoSugar {
        override lazy val applicationConfig = mockConfig
        override lazy val authConnector = mockAuthConnector
        override lazy val enrolmentConnector = mock[EnrolmentConnector]
        override lazy val s4lConnector = mock[S4LConnector]
        override lazy val acceptedFlows = Seq(Seq(EIS))
      }
      val enrolledUser = Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated")
      when(TestController.s4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

      when(AuthEnrolledTestController.enrolmentConnector.validateToken(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(true))

      when(TestController.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Option(enrolledUser)))
      val result = TestController.authorisedAsyncAction(authenticatedFakeRequest(AuthenticationProviderIds.GovernmentGatewayId))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Calling authenticated async action with valid not expired token and no schemeTypesModel is returned and controller only accepts specific schemes" should {
    "redirect to the application hub page" in {
      implicit val hc = HeaderCarrier()
      object TestController extends AuthEnrolledTestController with MockitoSugar {
        override lazy val applicationConfig = mockConfig
        override lazy val authConnector = mockAuthConnector
        override lazy val enrolmentConnector = mock[EnrolmentConnector]
        override lazy val s4lConnector = mock[S4LConnector]
        override lazy val acceptedFlows = Seq(Seq(EIS))
      }
      val enrolledUser = Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated")
      when(TestController.s4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

      when(AuthEnrolledTestController.enrolmentConnector.validateToken(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(true))

      when(TestController.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Option(enrolledUser)))
      val result = TestController.authorisedAsyncTokenAction(authenticatedFakeRequest(AuthenticationProviderIds.GovernmentGatewayId))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Calling authenticated async action and selected schemes are not valid for the controller" should {
    "redirect to the application hub page" in {
      implicit val hc = HeaderCarrier()
      object TestController extends AuthEnrolledTestController with MockitoSugar {
        override lazy val applicationConfig = mockConfig
        override lazy val authConnector = mockAuthConnector
        override lazy val enrolmentConnector = mock[EnrolmentConnector]
        override lazy val s4lConnector = mock[S4LConnector]
        override lazy val acceptedFlows = Seq(Seq(EIS))
      }
      val enrolledUser = Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated")
      when(TestController.s4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(seisSchemeTypesModel))

      when(AuthEnrolledTestController.enrolmentConnector.validateToken(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(true))

      when(TestController.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Option(enrolledUser)))
      val result = TestController.authorisedAsyncAction(authenticatedFakeRequest(AuthenticationProviderIds.GovernmentGatewayId))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }
}
