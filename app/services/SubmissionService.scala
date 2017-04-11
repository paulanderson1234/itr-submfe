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
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait SubmissionService {

  val submissionConnector: SubmissionConnector

  def getEtmpSubmissionDetails(tavcRef: String)(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[SubmissionDetailsModel]] = {
    submissionConnector.getAASubmissionDetails(tavcRef) map {
      submissionDetails =>
        submissionDetails.json.validate[SubmissionDetailsModel] match {
          case data: JsSuccess[SubmissionDetailsModel] =>
            Some(data.value)
          case e: JsError =>
            Logger.warn(s"[SubmissionService][getEtmpSubmissionDetails] - Failed to parse JSON response. Errors=${e.errors}")
            None
        }
    }
  }
}

object SubmissionService extends SubmissionService {
  val submissionConnector = SubmissionConnector
}
