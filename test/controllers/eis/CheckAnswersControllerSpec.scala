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

import auth.{Enrolment, Identifier, MockAuthConnector, MockConfig}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
<<<<<<< HEAD:test/controllers/eis/CheckAnswersControllerSpec.scala
import controllers.helpers.ControllerSpec
=======
import helpers.BaseSpec
>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/CheckAnswersControllerSpec.scala
import org.mockito.Matchers
import org.mockito.Mockito._
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
      investmentSetup(Some(proposedInvestmentModel),Some(usedInvestmentReasonBeforeModelYes),Some(previousBeforeDOFCSModelYes),
        Some(newGeographicalMarketModelYes),Some(newProductMarketModelYes),Some(subsidiariesSpendingInvestmentModelYes),Some(subsidiariesNinetyOwnedModelNo),
        Some(investmentGrowModel))
      contactDetailsSetup(Some(contactDetailsModel))
      contactAddressSetup(Some(contactAddressModel))
      companyDetailsSetup(Some(yourCompanyNeedModel),Some(taxpayerReferenceModel),Some(registeredAddressModel),Some(dateOfIncorporationModel),
        Some(natureOfBusinessModel),Some(commercialSaleModelYes),Some(isKnowledgeIntensiveModelYes),Some(operatingCostsModel),
        Some(percentageStaffWithMastersModelYes),Some(tenYearPlanModelYes),Some(subsidiariesModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(envelopeId))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to CheckAnswersController with an empty set of models when authenticated and enrolled" should {
    "return a 200 when the page is loaded" in {
      previousRFISetup()
      investmentSetup()
      contactDetailsSetup()
      companyDetailsSetup()
      contactAddressSetup()
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(envelopeId))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request (with envelope id None) to CheckAnswersController with an empty set of models when authenticated and enrolled" should {
    "return a 200 when the page is loaded" in {
      previousRFISetup()
      investmentSetup()
      contactDetailsSetup()
      companyDetailsSetup()
      contactAddressSetup()
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(None))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request (with empty envelope id) to CheckAnswersController with an empty set of models when authenticated and enrolled" should {
    "return a 200 when the page is loaded" in {
      previousRFISetup()
      investmentSetup()
      contactDetailsSetup()
      companyDetailsSetup()
      contactAddressSetup()
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(Some("")))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a submission to the CheckAnswersController" should {

    "redirect to the acknowledgement page when authenticated and enrolled" in {
      when(TestController.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Some(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.AcknowledgementController.show().url)
        }
      )
    }

<<<<<<< HEAD:test/controllers/eis/CheckAnswersControllerSpec.scala
  "Sending a timed-out request to CheckAnswersController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show(Some(enevleopId)))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the CheckAnswersController" should {

      "redirect to the acknowledgement page when authenticated and enrolled" in {
        when(TestController.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))
        submitWithSessionAndAuth(TestController.submit)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/acknowledgement")
          }
        )
      }

      "redirect to the subscription service when authenticated and NOT enrolled" in {
        mockNotEnrolledRequest()
        submitWithSessionAndAuth(TestController.submit)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
          }
        )
      }

      "redirect to the GG login page when having a session but not authenticated" in {
        submitWithSessionWithoutAuth(TestController.submit)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
              URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
            }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
          }
        )
      }

      "redirect to the GG login page with no session" in {
        submitWithoutSession(TestController.submit)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
              URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
            }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
          }
        )
      }

      "redirect to the Timeout page when session has timed out" in {
        submitWithTimeout(TestController.submit)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
          }
        )
      }
    }
=======
  }

>>>>>>> 790bbb8a2c7610e9682aaf069dc37315ab8a0b7f:test/controllers/CheckAnswersControllerSpec.scala
}
