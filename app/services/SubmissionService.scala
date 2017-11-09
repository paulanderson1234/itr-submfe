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

import auth.TAVCUser
import connectors.{S4LConnector, SubmissionConnector}
import models.submission.SubmissionDetailsModel
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess}

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

trait SubmissionService {

  val submissionConnector: SubmissionConnector

  def getEtmpReturnsSummary(tavcRef: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, user: TAVCUser): Future[Option[SubmissionDetailsModel]] = {
    submissionConnector.getReturnsSummary(tavcRef) map {
      submissionDetails =>
        submissionDetails.json.validate[SubmissionDetailsModel] match {
          case data: JsSuccess[SubmissionDetailsModel] =>
            Some(data.value)
          case e: JsError =>
            Logger.warn(s"[SubmissionService][getEtmpReturnsSummary] - Failed to parse JSON response. Errors=${e.errors}")
            None
        }
    }
  }

  def hasPreviousSubmissions(tavcRef: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, user: TAVCUser): Future[Boolean] = {
    getEtmpReturnsSummary(tavcRef) map {
      result => result.nonEmpty && result.get.submissions.fold(0)(_.length) > 0
    }
  }.recover{
    case e =>
      Logger.warn(s"[SubmissionService][hasPreviousSubmissions] - Error checking previous submission history. TavcRef: $tavcRef. Errors=$e")
      false
  }
}

object SubmissionService extends SubmissionService {
  val submissionConnector = SubmissionConnector
}
