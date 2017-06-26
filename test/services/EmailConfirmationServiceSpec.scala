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

package services

import auth.{MockConfig, TAVCUser, ggUser}
import common.KeystoreKeys
import config.FrontendAppConfig
import connectors.{EmailConfirmationConnector, S4LConnector}

import models.submission.SubmissionResponse
import models.{AddressModel, ContactDetailsModel}
import models.registration.RegistrationDetailsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerTest
import play.api.test.Helpers._

import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class EmailConfirmationServiceSpec extends UnitSpec with MockitoSugar with OneAppPerTest {

  object TestEmailConfirmationService extends EmailConfirmationService{
    override val s4LConnector: S4LConnector = mock[S4LConnector]
    override val emailConfirmationConnector: EmailConfirmationConnector = mock[EmailConfirmationConnector]
    override val registrationDetailsService: RegistrationDetailsService = mock[RegistrationDetailsService]
    override val emailTemplate: String = MockConfig.emailConfirmationTemplate
  }

  val validTavcReference = "XATAVC000123456"
  val submissionResponse = SubmissionResponse("01-01-9999", "XAFORMBUNDLE123456789")

  val fullCorrespondenceAddress: AddressModel = AddressModel(addressline1 = "line 1",
    addressline2 = "Line 2", addressline3 = Some("Line 3"), addressline4 = Some("Line 4"),
    postcode = Some("TTT 999"), countryCode = "GB")

  val registrationDetailsModel = RegistrationDetailsModel("Company ltd", fullCorrespondenceAddress)

  val contactDetailsValid = ContactDetailsModel("fred", "Smith", Some("01952 245666"), None, "fred@hotmail.com")



  val internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5"

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1234")))
  implicit val user: TAVCUser = TAVCUser(ggUser.allowedAuthContext, internalId)

  "The EmailConfirmationService" should {
    "use the correct s4l connector" in {
      EmailConfirmationService.s4LConnector shouldBe S4LConnector
    }
    "use the correct email confirmation connector" in {
      EmailConfirmationService.emailConfirmationConnector shouldBe EmailConfirmationConnector
    }
    "use the correct registration details service" in {
      EmailConfirmationService.registrationDetailsService shouldBe RegistrationDetailsService
    }
    "use the correct email template" in {
      EmailConfirmationService.emailTemplate shouldBe FrontendAppConfig.emailConfirmationTemplate
    }
  }
  "The NoDocsEmailConfirmationService" should {
    "use the correct s4l connector" in {
      NoDocsEmailConfirmationService.s4LConnector shouldBe S4LConnector
    }
    "use the correct email confirmation connector" in {
      NoDocsEmailConfirmationService.emailConfirmationConnector shouldBe EmailConfirmationConnector
    }
    "use the correct registration details service" in {
      NoDocsEmailConfirmationService.registrationDetailsService shouldBe RegistrationDetailsService
    }
    "use the correct email template" in {
      NoDocsEmailConfirmationService.emailTemplate shouldBe FrontendAppConfig.noDocsEmailConfirmationTemplate
    }
  }



  "Calling sendEmailConfirmation" when {
    "an email is successfully sent" should {
      lazy val result = TestEmailConfirmationService.sendEmailConfirmation(validTavcReference, submissionResponse)
      "return Accepted response" in {
        when(TestEmailConfirmationService.registrationDetailsService.getRegistrationDetails(Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(registrationDetailsModel)))
        when(TestEmailConfirmationService.s4LConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(contactDetailsValid)))
        when(TestEmailConfirmationService.emailConfirmationConnector.sendEmailConfirmation(Matchers.any())(Matchers.any())).
          thenReturn(Future.successful(HttpResponse(ACCEPTED)))
        await(result).status shouldBe ACCEPTED
      }
    }

    "an email is unsuccessful due to missing registration details" should {
      lazy val result = TestEmailConfirmationService.sendEmailConfirmation(validTavcReference, submissionResponse)
      "return INTERNAL_SERVER_ERROR" in {
        when(TestEmailConfirmationService.registrationDetailsService.getRegistrationDetails(Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        await(result).status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "an email is unsuccessful due to missing contact details" should {
      lazy val result = TestEmailConfirmationService.sendEmailConfirmation(validTavcReference, submissionResponse)
      "return INTERNAL_SERVER_ERROR" in {
        when(TestEmailConfirmationService.registrationDetailsService.getRegistrationDetails(Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(registrationDetailsModel)))
        when(TestEmailConfirmationService.s4LConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        await(result).status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "an email is unsuccessful due failed email connector call" should {
      lazy val result = TestEmailConfirmationService.sendEmailConfirmation(validTavcReference, submissionResponse)
      "return INTERNAL_SERVER_ERROR" in {
        when(TestEmailConfirmationService.registrationDetailsService.getRegistrationDetails(Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(registrationDetailsModel)))
        when(TestEmailConfirmationService.s4LConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(contactDetailsValid)))
        when(TestEmailConfirmationService.emailConfirmationConnector.sendEmailConfirmation(Matchers.any())(Matchers.any()))
          .thenReturn(Future.failed(Upstream5xxResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))
        await(result).status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
