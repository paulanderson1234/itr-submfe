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

package models.submission

import play.api.libs.json.Json

case class Scheme(scheme: String)

object Scheme{
  implicit val formats = Json.format[Scheme]
}

case class AASubmissionDetailsModel(formBundleNumber: String, submissionType: String,
                                    submissionDate: String, schemeType: List[Scheme],
                                    status: String, contactNoteReference: String)

object AASubmissionDetailsModel{
  implicit val formats = Json.format[AASubmissionDetailsModel]
}

case class SubmissionDetailsModel(submissions: List[AASubmissionDetailsModel])

object SubmissionDetailsModel{
  implicit val formats = Json.format[SubmissionDetailsModel]
}
