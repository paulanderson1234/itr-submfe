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
  val taxpayerReference: String = "companyDetails:taxpayerReference"
  val yourCompanyNeed: String = "introduction:yourCompanyNeed"
  val commercialSale: String = "companyDetails:commercialSale"
  val registeredAddress: String = "introduction:registeredAddress"
  val dateOfIncorporation: String = "companyDetails:dateOfIncorporation"
  val isKnowledgeIntensive: String = "companyDetails:isKnowledgeIntensive"
  val subsidiaries: String = "companyDetails:subsidiaries"
  val natureOfBusiness: String = "companyDetails:natureOfBusiness"
  val operatingCosts: String = "companyDetails:operatingCosts"
  val percentageStaffWithMasters: String = "knowledgeIntensive:percentageStaffWithMasters"
  val tenYearPlan: String = "knowledgeIntensive:tenYearPlan"
  val whatWillUseFor: String = "investment:whatWillUseFor"
  val proposedInvestment: String = "investment:proposedInvestment"
  val usedInvestmentReasonBefore: String = "investment:usedReasonBefore"
  val subsidiariesSpendingInvestment: String = "investment:subsidiariesSpendingInvestment"
  val newProduct: String = "investment:newProduct"
  val newGeographicalMarket: String = "investment:newGeographicalMarket"
  val subsidiariesNinetyOwned: String = "investment:subsidiariesNinetyOwned"
  val hadPreviousRFI: String = "previousInvestmentScheme:hadPreviousRFI"
  val previousBeforeDOFCS: String = "previousInvestmentScheme:previousBeforeDOFCS"
  val investmentGrow: String = "investment:investmentGrow"
  val confirmContactAddress: String = "contactInformation:confirmCorrespondAddress"
  val checkYourAnswers: String = "checkAndSubmit:checkYourAnswers"
  val contactDetails: String = "examples:contactDetails"
  val contactAddress: String = "contactInformation:contactAddress"
  val previousSchemes: String = "previousInvestmentScheme:previousInvestmentSchemes"

  // backlink keys
  val backLinkSupportingDocs: String = "backLink:SupportingDocs"
  val backLinkNewGeoMarket: String = "backLink:NewGeoMarket"
  val backLinkSubSpendingInvestment: String = "backLink:SubSpendingInvestment"
  val backLinkInvestmentGrow: String = "backLink:InvestmentGrow"
  val backLinkSubsidiaries: String = "backLink:subsidiaries"
  val backLinkPreviousScheme: String = "backLink:previousScheme"
  val backLinkProposedInvestment: String = "backLink:proposedInvestment"
  val backLinkIneligibleForKI: String = "backLink:IneligibleForKI"

}
