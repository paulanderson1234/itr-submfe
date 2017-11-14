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

package controllers.eisseis

import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class IneligibleForKIControllerSpec extends BaseSpec {

  object TestController extends IneligibleForKIController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(backLink: Option[String] = None): Unit=
    when(mockS4lConnector.fetchAndGetFormData[String]
      (Matchers.eq(KeystoreKeys.backLinkIneligibleForKI))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backLink))

  "IneligibleForKIController" should {
    "use the correct keystore connector" in {
      IneligibleForKIController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      IneligibleForKIController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      IneligibleForKIController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to IneligibleForKIController without a valid backlink from keystore when authenticated and enrolled" should {
    "redirect to the beginning of the flow" in {
      setupMocks()
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eisseis/operating-costs")
        }
      )
    }
  }

  "Sending a GET request to IneligibleForKIController with a valid back link when authenticated and enrolled" should {
    "return a 200" in {
      setupMocks(Some(routes.OperatingCostsController.show().url))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Posting to the IneligibleForKIController when authenticated and enrolled" should {
    "redirect to 'Subsidiaries' page" in {
      setupMocks(Some(routes.OperatingCostsController.show().url))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/eisseis/subsidiaries")
        }
      )
    }
  }
}
