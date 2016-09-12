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

package common

import connectors.SubmissionConnector
import models.{SubmissionResponse, YourCompanyNeedModel, ContactDetailsModel, SubmissionRequest}

object Constants extends Constants

trait Constants {
  val StandardRadioButtonYesValue = "Yes"
  val StandardRadioButtonNoValue = "No"
  def taxYearFormattedAnswer(value: String, taxYear: String) : String= s"£$value in $taxYear tax year"
  def amountFormattedAnswer(value: String) : String= s"£$value"
  val SuggestedTextMaxLength: Int = 2048

  val IsKnowledgeIntensiveYears : Int = 10
  val IsNotKnowledgeIntensiveYears : Int = 7
  val KI10Percent : Int = 10
  val KI15Percent : Int = 15

  val lifetimeLogicLimitKi : Int = 20000000
  val lifetimeLogicLimitNotKi : Int = 12000000

  val PageInvestmentSchemeEisValue : String = "Enterprise Investment Scheme"
  val PageInvestmentSchemeSeisValue : String = "Seed Enterprise Investment Scheme"
  val PageInvestmentSchemeSitrValue : String = "Social Investment Tax Relief"
  val PageInvestmentSchemeVctValue : String = "Venture Capital Trust"
  val PageInvestmentSchemeAnotherValue : String = "Another scheme"

  val dummySubmissionRequestModelValid = SubmissionRequest(ContactDetailsModel("James", "Harris", "0872990915","harris@gmail.com"),YourCompanyNeedModel("AA"))
  val dummySubmissionRequestModelBad = SubmissionRequest(ContactDetailsModel("James", "Harris", "0872990915","harris@badrequest.com"),YourCompanyNeedModel("AA"))
  val dummySubmissionRequestModelInternalServerError = SubmissionRequest(ContactDetailsModel("James", "Harris", "0872990915","harris@internalservererrorrequestgmail.com"),YourCompanyNeedModel("AA"))
  val dummySubmissionRequestModelForbidden = SubmissionRequest(ContactDetailsModel("James", "Harris", "0872990915","harris@forbiddengmail.com"),YourCompanyNeedModel("AA"))
  val dummySubmissionRequestModelServiceUnavailable = SubmissionRequest(ContactDetailsModel("James", "Harris", "0872990915","harris@serviceunavailablerequestgmail.com"),YourCompanyNeedModel("AA"))
  val dummySubmissionResponseModel = SubmissionResponse(true,"FBUND93821077","Submission Request Successful")
}
