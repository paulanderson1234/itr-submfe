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

import common.Constants
import forms.schemeSelection.SingleSchemeSelectionForm._
import play.api.libs.json.Json

/** TODO Remove when multiple scheme selection becomes available */
case class SingleSchemeTypesModel(schemeType: String)

object SingleSchemeTypesModel{

  implicit val formats =  Json.format[SingleSchemeTypesModel]

  def convertFromSingleScheme(singleSchemeTypesModel: SingleSchemeTypesModel): SchemeTypesModel = {
    singleSchemeTypesModel.schemeType match {
      case Constants.schemeTypeEis => SchemeTypesModel(eis = true)
      case Constants.schemeTypeSeis => SchemeTypesModel(seis = true)
      case Constants.schemeTypeVct => SchemeTypesModel(vct = true)
      case _ => SchemeTypesModel()
    }
  }

  def convertToSingleScheme(schemeTypesModel: SchemeTypesModel): SingleSchemeTypesModel = {
    schemeTypesModel match {
      case SchemeTypesModel(true,false,false,false) => SingleSchemeTypesModel(Constants.schemeTypeEis)
      case SchemeTypesModel(false,true,false,false) => SingleSchemeTypesModel(Constants.schemeTypeSeis)
      case SchemeTypesModel(false,false,false,true) => SingleSchemeTypesModel(Constants.schemeTypeVct)
    }
  }
}
