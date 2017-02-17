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

package common

object Constants extends Constants

trait Constants {
  val StandardRadioButtonYesValue = "Yes"
  val StandardRadioButtonNoValue = "No"
  def taxYearFormattedAnswer(value: String, taxYear: String) : String= s"£$value in $taxYear tax year"
  def amountFormattedAnswer(value: String) : String= s"£$value"
  val SuggestedTextMaxLength: Int = 2048
  val SuggestedTextMaxLengthLower: Int = 1024
  val SuggestetNatureOfBusinessLength: Int = 250

  val businessActivityPreparation = "Preparing To Trade"
  val businessActivityRAndD = "Research And Development"
  val businessActivityTrade = "Trade"

  val IsKnowledgeIntensiveYears : Int = 10
  val IsNotKnowledgeIntensiveYears : Int = 7
  val KI10Percent : Int = 10
  val KI15Percent : Int = 15


  val lifetimeLogicLimitKi : Int = 20000000
  val lifetimeLogicLimitNotKi : Int = 12000000
  val lifetimeLogicLimitKiToString : String = "£20"
  val lifetimeLogicLimitNotKiToString : String = "£12"

  val schemeTypeEis = "EIS"
  val schemeTypeSeis = "SEIS"
  val schemeTypeSitr = "SITR"
  val schemeTypeVct = "VCT"
  val schemeTypeOther = "Other"

  val PageInvestmentSchemeEisValue : String = "Enterprise Investment Scheme"
  val PageInvestmentSchemeSeisValue : String = "Seed Enterprise Investment Scheme"
  val PageInvestmentSchemeSitrValue : String = "Social Investment Tax Relief"
  val PageInvestmentSchemeVctValue : String = "Venture Capital Trust"
  val PageInvestmentSchemeAnotherValue : String = "Another scheme"

  val enrolmentOrgKey = "HMRC-TAVC-ORG"
  val enrolmentTavcRefKey = "TAVCRef"

  val standardIgnoreYearValue = "9999-12-31"
}
