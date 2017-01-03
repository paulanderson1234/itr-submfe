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
import utils.CostFormatter

case class OperatingCostsModel(operatingCosts1stYear : String, operatingCosts2ndYear : String, operatingCosts3rdYear : String,
                               rAndDCosts1stYear: String, rAndDCosts2ndYear : String, rAndDCosts3rdYear : String,
                               firstYear: String, secondYear: String, thirdYear: String)

object OperatingCostsModel extends CostFormatter{
  implicit val format = Json.format[OperatingCostsModel]
  implicit val writes = Json.writes[OperatingCostsModel]
}
