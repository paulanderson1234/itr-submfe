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

package models

import play.api.libs.json.Json

case class CommercialSaleModel(hasCommercialSale : String, day: Option[Int], month: Option[Int], year: Option[Int]){
  val toDate = if(day.isDefined && month.isDefined && year.isDefined) s"${day.get}-${month.get}-${year.get}" else ""
}

object CommercialSaleModel {
  implicit val format = Json.format[CommercialSaleModel]
  implicit val writes = Json.writes[CommercialSaleModel]
}
