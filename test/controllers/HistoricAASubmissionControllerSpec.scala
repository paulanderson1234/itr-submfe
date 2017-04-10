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

import auth.{MockAuthConnector, MockConfig}
import config.FrontendAuthConnector
import connectors.EnrolmentConnector
import controllers.helpers.BaseSpec
import models.submission.{SubmissionDetailsModel, Scheme, AASubmissionDetailsModel}
import org.mockito.Matchers
import play.api.test.Helpers._
import services.SubmissionService
import org.mockito.Mockito._

class HistoricAASubmissionControllerSpec extends BaseSpec{

  val tavcRef = "XATAVC000123456"
  val aASubmissionDetailsModelOne = AASubmissionDetailsModel("000000123456", "Compliance Statement",
    "2015-09-22", List(Scheme("EIS"),Scheme("UCT")), "Received", "003333333333")
  val aASubmissionDetailsModelTwo = AASubmissionDetailsModel("000000000000", "Advance Assurance",
    "2015-09-22", List(Scheme("EIS"),Scheme("UCT")), "Rejected", "003333333334")

  val combinedSubmissionModel = SubmissionDetailsModel(List(aASubmissionDetailsModelOne,aASubmissionDetailsModelTwo))

  val emptySubmissionModel = SubmissionDetailsModel(List())


  object TestController extends HistoricAASubmissionController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
    override val submissionService = mock[SubmissionService]
  }

  "HistoricAASubmissionController" should {
    "use the correct auth connector" in {
      HistoricAASubmissionController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      HistoricAASubmissionController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to HistoricAASubmissionController when authenticated and enrolled" should {
    "return a 200 OK when a submission details model is received" in {
      mockEnrolledRequest()
      when(TestController.submissionService.getEtmpSubmissionDetails(Matchers.any())(Matchers.any(),Matchers.any()))
        .thenReturn(Some(combinedSubmissionModel))
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "return a 500 INTERNAL_SERVER_ERROR when no submission details model is received" in {
      mockEnrolledRequest()
      when(TestController.submissionService.getEtmpSubmissionDetails(Matchers.any())(Matchers.any(),Matchers.any()))
        .thenReturn(None)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe INTERNAL_SERVER_ERROR
      )
    }

    "return a 500 INTERNAL_SERVER_ERROR when a error occurs" in {
      mockEnrolledRequest()
      when(TestController.submissionService.getEtmpSubmissionDetails(Matchers.any())(Matchers.any(),Matchers.any()))
          .thenThrow(new NullPointerException)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe INTERNAL_SERVER_ERROR
      )
    }
  }
}
