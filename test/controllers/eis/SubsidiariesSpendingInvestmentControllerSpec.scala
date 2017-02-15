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

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class SubsidiariesSpendingInvestmentControllerSpec extends BaseSpec {

  object SubsidiariesSpendingInvestmentControllerTest extends SubsidiariesSpendingInvestmentController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(subsidiariesSpendingInvestmentModel: Option[SubsidiariesSpendingInvestmentModel] = None, backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.any())
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(subsidiariesSpendingInvestmentModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubSpendingInvestment))(
      Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(backLink))
  }

  "SubsidiariesSpendingInvestmentController" should {
    "use the correct keystore connector" in {
      SubsidiariesSpendingInvestmentController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      SubsidiariesSpendingInvestmentController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      SubsidiariesSpendingInvestmentController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to SubsidiariesSpendingInvestmentController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(subsidiariesSpendingInvestmentModelYes), Some(routes.ProposedInvestmentController.show().url))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore when authenticated and enrolled" in {
      setupMocks(backLink = Some(routes.ProposedInvestmentController.show().url))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 300 when no back link is fetched using keystore when authenticated and enrolled" in {
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/proposed-investment")
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the SubsidiariesSpendingInvestmentController when authenticated and enrolled" should {
    "redirect to the subsidiaries-ninety-percent-owned page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "subSpendingInvestment" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/subsidiaries-ninety-percent-owned")
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the SubsidiariesSpendingInvestmentController when authenticated and enrolled" should {
    "redirect to the how-plan-to-use-investment page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "subSpendingInvestment" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/how-plan-to-use-investment")
        }
      )
    }
  }

  "Sending a invalid form submit to the SubsidiariesSpendingInvestmentController with no back link when authenticated and enrolled" should {
    "redirect to the subsidiaries-ninety-percent-owned page" in {
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "subSpendingInvestment" -> ""
      submitWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/proposed-investment")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the SubsidiariesSpendingInvestmentController when authenticated and enrolled" should {
    "redirect to itself with errors" in {
      setupMocks(backLink = Some(routes.ProposedInvestmentController.show().url))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "subSpendingInvestment" -> ""
      submitWithSessionAndAuth(SubsidiariesSpendingInvestmentControllerTest.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
