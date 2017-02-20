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

import models._
import play.api.libs.json.Json

case class SubmitMarketInfoModel(
                             newGeographicalMarketModel: NewGeographicalMarketModel, // required
                             newProductModel: NewProductModel, // required
                             marketDescription: Option[String] = None // not captured
                           )

case class SubsidiaryPerformingTradeModel(
                                          ninetyOwnedModel: SubsidiariesNinetyOwnedModel,
                                          organisationName: String,
                                          ctUtr: Option[String] = None,
                                          crn: Option[String] = None,
                                          companyAddress: Option[AddressModel] = None
                                    )

case class OrganisationDetailsModel(
                                organisationName: String,
                                utr: Option[String] = None,
                                chrn: Option[String] = None,
                                startDate: DateOfIncorporationModel,
                                firstDateOfCommercialSale: Option[String] = None,
                                ctUtr: Option[String] = None,
                                crn: Option[String] = None,
                                companyAddress: Option[AddressModel] = None,
                                previousRFIs: Option[Seq[PreviousSchemeModel]]
                              )

case class AdvancedAssuranceSubmissionType(
                                            // minimum mandatory types required:
                                            natureOfBusinessModel: NatureOfBusinessModel, //trade: baDescription
                                            contactDetailsModel: ContactDetailsModel,
                                            proposedInvestmentModel: ProposedInvestmentModel,
                                            investmentGrowModel: InvestmentGrowModel,
                                            organisationDetails: OrganisationDetailsModel,

                                            // mandatory types either statically generated or hard-coded until captured
                                            organisationType: String = "Limited", // always limited for AA submission
                                            // required for contactName (name1 only mandatory) and and contactDetails.
                                            // if mobileNumber and/or faxNumber are added to this model later they will
                                            // be automatically read by target model
                                            correspondenceAddress: AddressModel,
                                            schemeTypes: SchemeTypesModel,

                                            // optional types:
                                            whatWillUseForModel: Option[WhatWillUseForModel] =
                                              Some(WhatWillUseForModel(None)), //trade:businessActivity
                                            marketInfo: Option[SubmitMarketInfoModel], // trade: Market info
                                            dateTradeCommenced: String,
                                            // converted from from operating costs model with dynamic period generated:
                                            annualCosts: Option[Seq[AnnualCostModel]],
                                            // derive from captured turnover when done.
                                            // probably follow annualCosts pattern then to populate
                                            annualTurnover: Option[Seq[TurnoverCostModel]],
                                            acknowledgementReference: Option[String] = None, // generated at back end
                                            agentReferenceNumber: Option[String], //TODO: Where from?
                                            //TODO: prev owner element if required,
                                            // derives from KIProcessingMoel - only populate if companyAssertKi is true
                                            knowledgeIntensive: Option[KiModel],
                                            subsidiaryPerformingTrade: Option[SubsidiaryPerformingTradeModel]

                                          )
case class Submission(
                       submission : AdvancedAssuranceSubmissionType
                     )

object Submission {
  implicit val formatOrgDetails = Json.format[OrganisationDetailsModel]
  implicit val formatSubsidiaryPerfomingTrade = Json.format[SubsidiaryPerformingTradeModel]
  implicit val formatSubmiKitModel = Json.format[KiModel]
  implicit val formatSubmitCostModel = Json.format[CostModel]
  implicit val formatSubmitAnnualCostModel = Json.format[AnnualCostModel]
  implicit val formatSubmitTurnoverCostModel = Json.format[TurnoverCostModel]
  implicit val formatSubmitMarketInfo = Json.format[SubmitMarketInfoModel]
  implicit val formatSubSubmissionType = Json.format[AdvancedAssuranceSubmissionType]
  implicit val formatSubSubmission = Json.format[Submission]
}
