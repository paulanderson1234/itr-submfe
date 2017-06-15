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

package models

import common.Constants
import play.api.libs.json.Json
import utils.DateFormatter

case class EmailConfirmationModel(to: Array[String], templateId: String, parameters: Map[String,String] = Map(),
                                  force: Boolean = false, eventUrl: Option[String] = None, onSendUrl: Option[String] = None)

object EmailConfirmationModel extends DateFormatter{

  implicit val formats = Json.format[EmailConfirmationModel]

  def parameters(companyName: String, formBundleRef: String): Map[String,String] = {
    val emailConstants = Constants.EmailConfirmationParameters
    Map(emailConstants.companyName -> companyName, emailConstants.date -> desDateToDateString, emailConstants.formBundleRefNUmber -> formBundleRef)
  }

}
