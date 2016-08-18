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
object KeystoreKeys extends KeystoreKeys

trait KeystoreKeys {
  // form keys
  val taxpayerReference = "companyDetails:taxpayerReference"
  val yourCompanyNeed = "introduction:yourCompanyNeed"
  val commercialSale = "companyDetails:commercialSale"
  val registeredAddress ="introduction:registeredAddress"
  val dateOfIncorporation = "companyDetails:dateOfIncorporation"
  val isKnowledgeIntensive = "companyDetails:isKnowledgeIntensive"
  val subsidiaries = "companyDetails:subsidiaries"
  val natureOfBusiness = "companyDetails:natureOfBusiness"
  val operatingCosts = "companyDetails:operatingCosts"
  val percentageStaffWithMasters ="knowledgeIntensive:percentageStaffWithMasters"
  val tenYearPlan = "knowledgeIntensive:tenYearPlan"
  val whatWillUseFor ="investment:whatWillUseFor"
  val proposedInvestment = "investment:proposedInvestment"
  val usedInvestmentReasonBefore = "investment:usedReasonBefore"
  val subsidiariesSpendingInvestment ="investment:subsidiariesSpendingInvestment"
  val newProduct = "investment:newProduct"
  val newGeographicalMarket = "investment:newGeographicalMarket"
  val subsidiariesNinetyOwned="investment:subsidiariesNinetyOwned"
  val hadPreviousRFI ="previousInvestmentScheme:hadPreviousRFI"
  val previousBeforeDOFCS ="previousInvestmentScheme:previousBeforeDOFCS"
  val investmentGrow ="investment:investmentGrow"
  val confirmContactAddress = "contactInformation:confirmCorrespondAddress"
  val checkYourAnswers ="checkAndSubmit:checkYourAnswers"
  val contactDetails = "examples:contactDetails"
  val contactAddress = "contactInformation:contactAddress"

  // backlink keys
  val backLinkSupportingDocs ="backLink:SupportingDocs"
  val backLinkNewGeoMarket ="backLink:NewGeoMarket"
  val backLinkSubSpendingInvestment ="backLink:SubSpendingInvestment"
  val backLinkInvestmentGrow ="backLink:InvestmentGrow"
}
