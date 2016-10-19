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

import java.net.URLEncoder

import auth.{Enrolment, Identifier, MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import helpers.FakeRequestHelper
import models.{ContactDetailsModel, _}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.mock.MockitoSugar
import views.helpers.CheckAnswersSpec

import scala.concurrent.Future

class CheckAnswersControllerSpec extends UnitSpec with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper with CheckAnswersSpec {

  object CheckAnswersControllerTest extends CheckAnswersController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  private def mockEnrolledRequest = when(CheckAnswersControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Some(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(CheckAnswersControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))


  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockS4lConnector)
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
      investmentSetup(Some(proposedInvestmentModel),Some(whatWillUseForModel),Some(usedInvestmentReasonBeforeModelYes),Some(previousBeforeDOFCSModelYes),
        Some(newGeographicalMarketModelYes),Some(newProductMarketModelYes),Some(subsidiariesSpendingInvestmentModelYes),Some(subsidiariesNinetyOwnedModelNo),
        Some(investmentGrowModel))
      contactDetailsSetup(Some(contactDetailsModel))
      companyDetailsSetup(Some(yourCompanyNeedModel),Some(taxpayerReferenceModel),Some(registeredAddressModel),Some(dateOfIncorporationModel),
        Some(natureOfBusinessModel),Some(commercialSaleModelYes),Some(isKnowledgeIntensiveModelYes),Some(operatingCostsModel),
        Some(percentageStaffWithMastersModelYes),Some(tenYearPlanModelYes),Some(subsidiariesModelYes))
      mockEnrolledRequest

      showWithSessionAndAuth(CheckAnswersControllerTest.show())(
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
      mockEnrolledRequest

      showWithSessionAndAuth(CheckAnswersControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending an Authenticated and NOT Enrolled GET request with a session to CheckAnswersControllerTest" should {
    "redirect to the TAVC Subscription Service" in {
      previousRFISetup()
      investmentSetup()
      contactDetailsSetup()
      companyDetailsSetup()
      mockNotEnrolledRequest

      showWithSessionAndAuth(CheckAnswersControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to CheckAnswersController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(CheckAnswersControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to CheckAnswersController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(CheckAnswersControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to CheckAnswersController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(CheckAnswersControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the CheckAnswersController" should {

      "redirect to the acknowledgement page when authenticated and enrolled" in {
        when(CheckAnswersControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))
        submitWithSessionAndAuth(CheckAnswersControllerTest.submit)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/acknowledgement")
          }
        )
      }

      "redirect to the subscription service when authenticated and NOT enrolled" in {
        mockNotEnrolledRequest
        submitWithSessionAndAuth(CheckAnswersControllerTest.submit)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
          }
        )
      }

      "redirect to the GG login page when having a session but not authenticated" in {
        submitWithSessionWithoutAuth(CheckAnswersControllerTest.submit)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
              URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
            }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
          }
        )
      }

      "redirect to the GG login page with no session" in {
        submitWithoutSession(CheckAnswersControllerTest.submit)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
              URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
            }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
          }
        )
      }

      "redirect to the Timeout page when session has timed out" in {
        submitWithTimeout(CheckAnswersControllerTest.submit)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
          }
        )
      }
    }
}
