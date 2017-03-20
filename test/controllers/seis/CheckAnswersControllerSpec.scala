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
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.test.Helpers._
import views.helpers.CheckAnswersSpec

import scala.concurrent.Future

class CheckAnswersControllerSpec extends BaseSpec with CheckAnswersSpec {
  
  object TestController extends CheckAnswersController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "CheckAnswersController" should {
    "use the correct auth connector" in {
      CheckAnswersController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      CheckAnswersController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      CheckAnswersController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to CheckAnswersController with a populated set of models when authenticated and enrolled" should {
    "return a 200 when the page is loaded" in {
      previousRFISetup(Some(hadPreviousRFIModelYes))
      seisInvestmentSetup(Some(proposedInvestmentModel), Some(subsidiariesSpendingInvestmentModelNo), Some(subsidiariesNinetyOwnedModelNo))
      contactDetailsSetup(Some(contactDetailsModel))
      contactAddressSetup(Some(contactAddressModel))
      seisCompanyDetailsSetup(Some(registeredAddressModel), Some(dateOfIncorporationModel),
        Some(natureOfBusinessModel), Some(subsidiariesModelYes), Some(tradeStartDateModelYes))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(envelopeId))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to CheckAnswersController with an empty set of models when authenticated and enrolled" should {
    "return a 200 when the page is loaded" in {
      previousRFISetup()
      seisInvestmentSetup()
      contactDetailsSetup()
      seisCompanyDetailsSetup()
      contactAddressSetup()
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(envelopeId))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request (with envelope id None) to CheckAnswersController with an empty set of models when authenticated and enrolled" should {
    "return a 200 when the page is loaded" in {
      previousRFISetup()
      seisInvestmentSetup()
      contactDetailsSetup()
      seisCompanyDetailsSetup()
      contactAddressSetup()
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(None))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request (with empty envelope id) to CheckAnswersController with an empty set of models when authenticated and enrolled" should {
    "return a 200 when the page is loaded" in {
      previousRFISetup()
      seisInvestmentSetup()
      contactDetailsSetup()
      seisCompanyDetailsSetup()
      contactAddressSetup()
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(Some("")))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a submission to the CheckAnswersController with one or more attachments for SEIS" should {

    "redirect to the acknowledgement page when authenticated and enrolled" in {
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeId))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some("test")))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.AttachmentsAcknowledgementController.show().url)
        }
      )
    }
  }

  "Sending a submission to the CheckAnswersController with no attachments for SEIS" should {

    "redirect to the acknowledgement page when authenticated and enrolled" in {
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeId))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.AcknowledgementController.show().url)
        }
      )
    }
  }
}
