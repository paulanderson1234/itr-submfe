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

case class Scheme(scheme: Option[String])

object Scheme{
  implicit val formats = Json.format[Scheme]
}

case class AASubmissionDetailsModel(formBundleNumber: Option[String], submissionType: Option[String],
                                    submissionDate: Option[String], schemeType: Option[List[Scheme]],
                                    status: Option[String], contactNoteReference: Option[String])

object AASubmissionDetailsModel{
  implicit val formats = Json.format[AASubmissionDetailsModel]
}

case class SubmissionDetailsModel(submissions: Option[List[AASubmissionDetailsModel]])

object SubmissionDetailsModel{
  implicit val formats = Json.format[SubmissionDetailsModel]
}
