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

import auth.AuthorisedAndEnrolledForTAVC
import config.FrontendGlobal._
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import play.Logger
import services.SubmissionService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import utils.DateFormatter
import views.html.historicSubmissions.HistoricAASubmission



object  HistoricAASubmissionController extends HistoricAASubmissionController
{
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val s4lConnector = S4LConnector
  override val submissionService = SubmissionService
}

trait HistoricAASubmissionController extends FrontendController with AuthorisedAndEnrolledForTAVC with DateFormatter{

  override val acceptedFlows = Seq()
  val submissionService: SubmissionService

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    (for{
      tavcRef <- getTavCReferenceNumber()
      submissionDetails <- submissionService.getEtmpSubmissionDetails(tavcRef)
    } yield Ok(HistoricAASubmission(submissionDetails.get.submissions.map{
      submission => submission.copy(submissionDate = etmpDateToDateString(submission.submissionDate))
    }))).recover{
      case e: Exception => {
        Logger.warn(s"[HistoricAASubmissionController][show] - Exception retrieving historic AA submissions: ${e.getMessage}")
        InternalServerError(internalServerErrorTemplate)
      }
    }

  }

}
