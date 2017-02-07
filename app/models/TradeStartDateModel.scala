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
import utils.DateFormatter

case class TradeStartDateModel(hasTradeStartDate : String, tradeStartDay: Option[Int],
                               tradeStartMonth: Option[Int], tradeStartYear: Option[Int]){
  val toDate = if(tradeStartDay.isDefined && tradeStartMonth.isDefined &&
    tradeStartYear.isDefined) s"${tradeStartDay.get}-${tradeStartMonth.get}-${tradeStartYear.get}"
  else ""
}

object TradeStartDateModel extends DateFormatter{
  implicit val format = Json.format[TradeStartDateModel]
  implicit val writes = Json.writes[TradeStartDateModel]
}
