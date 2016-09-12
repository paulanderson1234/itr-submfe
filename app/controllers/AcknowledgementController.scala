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

import common.Constants
import connectors.SubmissionConnector
import controllers.predicates.ValidActiveSession
import models.{SubmissionResponse}
import uk.gov.hmrc.play.frontend.controller.FrontendController


object AcknowledgementController extends AcknowledgementController

trait AcknowledgementController extends FrontendController with ValidActiveSession {
  val show = ValidateSession.async { implicit request =>
    /** Dummy implementation. Will be replaced by final Submission model**/
    val submissionResponseModel = SubmissionConnector.submitAdvancedAssurance(Constants.dummySubmissionRequestModelValid)
    submissionResponseModel.map { submissionResponse =>
      Ok(views.html.checkAndSubmit.Acknowledgement(submissionResponse.json.as[SubmissionResponse]))
    }
  }
}
