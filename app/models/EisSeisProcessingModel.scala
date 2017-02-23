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

import play.api.libs.json.Json

/** Facilitates process handling in a combined EIS/SEIS flow
  *
  *  @constructor create a new EisSeisProcessingModel with 'Nobe' values which indicate the data has none been entered yet.
  *  @param ineligibleTradeStartCondition indicates whether the trade start condition has made SEIS ineligible
  *  @param ineligiblePreviousSchemeTypeCondition indicates whether the previous scheme types have made SEIS ineligible
  *  @param ineligiblePreviousSchemeThresholdCondition indicates whether the previous scheme threshold has made SEIS ineligible
  */
case class EisSeisProcessingModel(
                                   ineligibleTradeStartCondition : Option[Boolean] = None,
                                   ineligiblePreviousSchemeTypeCondition : Option[Boolean] = None,
                                   ineligiblePreviousSchemeThresholdCondition : Option[Boolean] = None
                            ){
  val isSeisIneligible: Boolean =
    ineligibleTradeStartCondition.getOrElse(false)||
      ineligiblePreviousSchemeTypeCondition.getOrElse(false)||
      ineligiblePreviousSchemeThresholdCondition.getOrElse(false)
}

object EisSeisProcessingModel {
  implicit val format = Json.format[EisSeisProcessingModel]
  implicit val writes = Json.writes[EisSeisProcessingModel]
}
