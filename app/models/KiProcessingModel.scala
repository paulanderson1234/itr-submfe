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

/** Facilitates handling Konwledge Intensive (KI) interactions withing the application..
  *
  *  @constructor create a new KiProcessingModel with 'Nobe' values which indicate the data has none been entered yet.
  *  @param companyAssertsIsKi the company has indicated whether or not they are KI
  *  @param dateConditionMet indicates whether the date of incorporation satisfies the KI condition
  *  @param costsConditionMet indicates whether the costs (operating/R&D) satisfy the KI condition (from API call result)
  *  @param hasPercentageWithMasters company indicated they had at lwast twenty percent with masters
  *  @param hasTenYearPlan company indicated they had a ten year plan
  *  @param secondaryCondtionsMet indicates if the secondry conditions to be KI hve been met (from API call result)
  */
case class KiProcessingModel(
                              companyAssertsIsKi : Option[Boolean] = None,
                              dateConditionMet : Option[Boolean] = None,
                              costsConditionMet : Option[Boolean] = None,
                              hasPercentageWithMasters : Option[Boolean] = None,
                              hasTenYearPlan : Option[Boolean] = None,
                              secondaryCondtionsMet : Option[Boolean] = None
                            ){
  val isKi: Boolean = if(companyAssertsIsKi.getOrElse(false) &&
    dateConditionMet.getOrElse(false) &&
    costsConditionMet.getOrElse(false) &&
    secondaryCondtionsMet.getOrElse(false)) true else false
}

object KiProcessingModel {
  implicit val format = Json.format[KiProcessingModel]
  implicit val writes = Json.writes[KiProcessingModel]
}
