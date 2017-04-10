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

import play.api.i18n.Messages
import play.api.libs.json.Json
import utils.{CostFormatter, DateFormatter}
import common.Constants
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

case class PreviousSchemeModel (schemeTypeDesc : String,
                                investmentAmount : Int,
                                investmentSpent: Option[Int],
                                otherSchemeName: Option[String],
                                day: Option[Int],
                                month: Option[Int],
                                year: Option[Int],
                                processingId: Option[Int]
                               )

object PreviousSchemeModel extends DateFormatter with CostFormatter {
  implicit val format = Json.format[PreviousSchemeModel]
  implicit val writes = Json.writes[PreviousSchemeModel]

  def toArrayString(previousSchemeModel: PreviousSchemeModel): Array[String] = {
    val investmentSpent = s"${Messages("page.investment.amount.label")} ${getAmountAsFormattedString(previousSchemeModel.investmentAmount)}"
    val investmentAmount = if(previousSchemeModel.investmentSpent.isDefined)
      s"${Messages("page.investment.amountSpent.label")} ${getAmountAsFormattedString(previousSchemeModel.investmentSpent.get)}" else ""
    val dateOfIssue = if(previousSchemeModel.day.isDefined && previousSchemeModel.month.isDefined && previousSchemeModel.year.isDefined)
      s"${Messages("page.investment.dateOfShareIssue.label")} ${toDateString(previousSchemeModel.day.get,previousSchemeModel.month.get,
        previousSchemeModel.year.get)}" else ""
    (investmentAmount.isEmpty, dateOfIssue.isEmpty) match {
      case (true,true) => Array(investmentSpent)
      case (true,false) =>  Array(investmentSpent,dateOfIssue)
      case (false,true) => Array(investmentSpent,investmentAmount)
      case (false,false) => Array(investmentSpent,investmentAmount,dateOfIssue)
    }
  }

  def getSchemeName(schemeType: String, otherSchemeName: Option[String] = None): String = {
    schemeType match {
      case Constants.schemeTypeEis => Messages("page.previousInvestment.schemeType.eis")
      case Constants.schemeTypeSeis => Messages("page.previousInvestment.schemeType.seis")
      case Constants.schemeTypeSitr => Messages("page.previousInvestment.schemeType.sitr")
      case Constants.schemeTypeVct => Messages("page.previousInvestment.schemeType.vct")
      case _ => otherSchemeName.fold("")(_.self)
    }
  }

  def concatSchemeNames(schemeTypes: List[String]): String = {
    val join = "and"
    schemeTypes.map {
      schemeType => getSchemeName(schemeType)}
      .mkString(", ")
      .reverse
      .replaceFirst(" ,", " " + join.reverse + " ")
      .reverse
  }
}
