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

case class CheckAnswersModel(
                              yourCompanyNeedModel: Option[YourCompanyNeedModel],
                              taxpayerReferenceModel: Option[TaxpayerReferenceModel],
                              registeredAddressModel: Option[RegisteredAddressModel],
                              dateOfIncorporationModel: Option[DateOfIncorporationModel],
                              natureOfBusinessModel: Option[NatureOfBusinessModel],
                              commercialSaleModel: Option[CommercialSaleModel],
                              isKnowledgeIntensiveModel: Option[IsKnowledgeIntensiveModel],
                              operatingCostsModel: Option[OperatingCostsModel],
                              percentageStaffWithMastersModel: Option[PercentageStaffWithMastersModel],
                              tenYearPlanModel: Option[TenYearPlanModel],
                              subsidiariesModel: Option[SubsidiariesModel],
                              hadPreviousRFIModel: Option[HadPreviousRFIModel],
                              previousSchemes: Vector[PreviousSchemeModel],
                              proposedInvestmentModel: Option[ProposedInvestmentModel],
                              usedInvestmentReasonBeforeModel: Option[UsedInvestmentReasonBeforeModel],
                              previousBeforeDOFCSModel: Option[PreviousBeforeDOFCSModel],
                              newGeographicalMarketModel: Option[NewGeographicalMarketModel],
                              newProductModel: Option[NewProductModel],
                              subsidiariesSpendingInvestmentModel: Option[SubsidiariesSpendingInvestmentModel],
                              subsidiariesNinetyOwnedModel: Option[SubsidiariesNinetyOwnedModel],
                              contactDetailsModel: Option[ContactDetailsModel],
                              contactAddressModel: Option[AddressModel],
                              investmentGrowModel: Option[InvestmentGrowModel],
                              attachmentsEnabled: Boolean
                              )
