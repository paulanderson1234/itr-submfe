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

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.ContactDetailsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.EmailVerificationService

import scala.concurrent.Future

class EmailVerificationControllerSpec extends BaseSpec {

  object TestController extends EmailVerificationController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override val emailVerificationService = mock[EmailVerificationService]
  }

  "EmailVerificationController" should {
    "use the correct auth connector" in {
      EmailVerificationController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      EmailVerificationController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      EmailVerificationController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to EmailVerificationController" should {

    "return a 200 when EMAIL is not verified" in {
      when(TestController.emailVerificationService.sendVerificationLink(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(true))
      when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))
        (Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Some(contactDetailsModel)))
      when(TestController.emailVerificationService.verifyEmailAddress(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Some(false)))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.verify(Constants.ContactDetailsReturnUrl))(
        result => status(result) shouldBe OK
      )
    }

    "redirect to the ConfirmCorrespondAddress Controller page if email verified" in {
      when(TestController.emailVerificationService.verifyEmailAddress(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Some(true)))
      when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))
        (Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Some(contactDetailsModel)))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.verify(Constants.ContactDetailsReturnUrl))(
        result => redirectLocation(result) shouldBe Some(routes.ConfirmCorrespondAddressController.show().url)
      )
    }

    "redirect to the CheckAnswers Controller page if email verified" in {
      when(TestController.emailVerificationService.verifyEmailAddress(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Some(true)))
      when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))
        (Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Some(contactDetailsModel)))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.verify(Constants.CheckAnswersReturnUrl))(
        result => redirectLocation(result) shouldBe Some(routes.CheckAnswersController.show().url)
      )
    }
  }
}
