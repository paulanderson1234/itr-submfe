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

package controllers

import auth._
import common.Constants
import config.FrontendAuthConnector
import connectors.{ComplianceStatementConnector, EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.internal.CSApplicationModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future
import uk.gov.hmrc.http.HttpResponse

class ConfirmDeleteCSApplicationControllerSpec extends BaseSpec {


  object TestController extends ConfirmDeleteCSApplicationController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val complianceStatementConnector = mockComplianceStatementConnector
  }

  "ConfirmDeleteCSApplicationController" should {
    "use the correct auth connector" in {
      ConfirmDeleteCSApplicationController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      ConfirmDeleteCSApplicationController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      ConfirmDeleteCSApplicationController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct registration service" in {
      ConfirmDeleteCSApplicationController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct service service" in {
      ConfirmDeleteCSApplicationController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct compliance statement connector" in {
      ConfirmDeleteCSApplicationController.complianceStatementConnector shouldBe ComplianceStatementConnector
    }
  }

  "Sending a GET request to ConfirmDeleteCSApplicationController" should {
    "return a OK when CS Application in progress for a eis scheme fetched from storage" in {
      mockEnrolledRequest()
      when(TestController.complianceStatementConnector.getComplianceStatementApplication()
      (Matchers.any())).thenReturn(Future.successful(CSApplicationModel(true, Some(Constants.schemeTypeEis))))
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Sending a GET request to ConfirmDeleteApplicationController" should {
    "return a OK when CS Application in progress for a seis scheme fetched from storage" in {
      mockEnrolledRequest()
      when(TestController.complianceStatementConnector.getComplianceStatementApplication()
      (Matchers.any())).thenReturn(Future.successful(CSApplicationModel(true, Some(Constants.schemeTypeSeis))))
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Sending a GET request to ConfirmDeleteApplicationController" should {
    "REDIRECT when no cs application is found to be in progress" in {
      mockEnrolledRequest()
      when(TestController.complianceStatementConnector.getComplianceStatementApplication()
      (Matchers.any())).thenReturn(Future.successful(CSApplicationModel(false, None)))
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
        }
      )
    }
  }

  "Posting the confirm delete button on the ConfirmDeleteApplicationController" should {
    "redirect to hub page" in {
      when(mockComplianceStatementConnector.deleteComplianceStatementApplication()
      (Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.delete)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
        }
      )
    }
  }

}
