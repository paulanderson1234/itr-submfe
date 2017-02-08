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

import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils.Validation
import utils.Transformers._

case class DesCostModel(
                      amount : String,
                      currency: String = "GBP"
                    )

case class ProposedAmount (
                            amount: String,
                            currency: String = "GBP"
)

case class SubmitDesContactName(
                              name1: String,
                              name2: String
                            )

case class SubmitDesMarketInfo(
                       newGeographicMarket: Boolean,
                       newProductMarket: Boolean,
                       marketDescription: Option[String]
                     )

case class SubmitDesContactDetails(
                                    phoneNumber: Option[String],
                                    mobileNumber: Option[String],
                                    faxNumber: Option[String],
                                    emailAddress: Option[String]
                               )

case class SubmitDesAddressType(
                                 addressLine1: String,
                                 addressLine2: String,
                                 addressLine3: Option[String],
                                 addressLine4: Option[String],
                                 postalCode: Option[String],
                                 countryCode: String
                               )

case class SubmitDesCompanyDetailsType(
                                    organisationName: String,
                                    ctUtr:Option[String],
                                    crn:Option[String],
                                    //TODO: address does not exist - provided provisional read writes for when it does
                                    companyAddress: Option[SubmitDesAddressType]
                                  )

case class SubmitDesOrganisation(
                                  utr:Option[String],
                                  chrn:Option[String],
                                  startDate:String,
                                  firstDateOfCommercialSale:Option[String],
                                  orgDetails:SubmitDesCompanyDetailsType,
                                  previousRFIs: Option[RFICostsModel]
                                )

case class SubmitDesSubsidiaryPerformingTrade(
                                               ninetyPercentOwned: Boolean,
                                               companyDetails: SubmitDesCompanyDetailsType
                                             )

case class SubmitDesCorrespondenceDetails(
                                         contactName: SubmitDesContactName,
                                         contactDetails: SubmitDesContactDetails,
                                         contactAddress: SubmitDesAddressType
                                       )
case class AnnualTurnoversModel(
                                 nodata:Option[String],
                                 annualTurnover: Seq[TurnoverCostModel]
                               )

case class AnnualCostsModel(
                             nodata:Option[String],
                             annualCost: Seq[AnnualCostModel]
                           )

case class DateFromModel(
                          issueDate: String
                        )

case class RFIModel(
                           schemeType: String,
                           name: Option[String],
                           issueDate: String,
                           amount: DesCostModel,
                           amountSpent: Option[DesCostModel]
                         )

case class RFICostsModel(
                             nodata:Option[String],
                             previousRFI: Seq[RFIModel]
                           )

case class SubmitDesTradeModel(

                              businessActivity: Option[String],
                              baDescription: String,
                              marketInfo: Option[SubmitDesMarketInfo],
                              dateTradeCommenced: String,
                              annualCosts: Option[AnnualCostsModel],
                              annualTurnover:  Option[AnnualTurnoversModel]
                              //TODO: Add previousOwnership + structures when in scope (is optional so can writeNullable)
                            )

case class SubmitDesProposedInvestment(
                                        growthJustification: String,
                                        unitType: String,
                                        investmentAmount: ProposedAmount
                                      )

case class SubmitDesSubmission(
                                notRequired: Option[String],
                                advancedAssurance: SubmitDesAdvancedAssurance
                              )


case class SubmitDesAdvancedAssurance(
                                        schemeTypes: SchemeTypesModel,
                                        trade: SubmitDesTradeModel,
                                        proposedInvestment: SubmitDesProposedInvestment,
                                        subsidiaryPerformingTrade: Option[SubmitDesSubsidiaryPerformingTrade],
                                        knowledgeIntensive: Option[KiModel],
                                        organisation: SubmitDesOrganisation
                                      )

case class SubmitDesSubmissionType(
                                 agentReferenceNumber: Option[String],
                                 correspondenceDetails: SubmitDesCorrespondenceDetails,
                                 organisationType: String,
                                 submission: SubmitDesSubmission
                               )


case class DesSubmitAdvancedAssuranceModel(
                                 acknowledgementReference: Option[String] = None,
                                 submissionType: SubmitDesSubmissionType
                               )

object DesSubmitAdvancedAssuranceModel {

  def answerToBoolean(input:String): Boolean = {
    input.toLowerCase match {
      case "yes" => true
      case _ => false
    }
  }

  implicit val costModelWrites = Json.writes[CostModel]
  implicit val costsModelReads: Reads[CostModel] = (
    (__ \ "amount").read[String].map(amount => poundToPence(Left(amount))) and
      Reads.pure("GBP")
    ) (CostModel.apply _)

  implicit val kiModelFormat = Json.format[KiModel]

  implicit val desModelWrites = Json.writes[DesCostModel]
  implicit val desModelReads: Reads[DesCostModel] = (
    __.read[Int].map(amount => poundToPence(Right(amount))) and
      Reads.pure("GBP")
    ) (DesCostModel.apply _)

  //implicit val issueDateWrites = Json.writes[DateFromModel].transform(_.\("issueDate"))
  val dateReads: Reads[String] =
    for {
      day <- (__ \ "day").read[Int]
      month <- (__ \ "month").read[Int]
      year <- (__ \ "year").read[Int]
    } yield Validation.dateToDesFormat(day, month, year)

  implicit val formatCaWrites = Json.writes[SubmitDesAddressType]

  implicit val formatCaReads: Reads[SubmitDesAddressType] = (
    (__ \ "addressline1").read[String] and
      (__ \ "addressline2").read[String] and
      (__ \ "addressline3").readNullable[String] and
      (__ \ "addressline4").readNullable[String] and
      (__ \ "postcode").readNullable[String] and
      (__ \ "countryCode").read[String]
    ) (SubmitDesAddressType.apply _)

  implicit val companyDetailsTypeWrites = Json.writes[SubmitDesCompanyDetailsType]
  implicit val companyDetailsTypeReads: Reads[SubmitDesCompanyDetailsType] = (
    (__ \ "organisationName").read[String] and
      (__ \ "ctUtr").readNullable[String] and
      (__ \ "crn").readNullable[String] and
      (__ \ "companyAddress").readNullable[SubmitDesAddressType]
    ) (SubmitDesCompanyDetailsType.apply _)

  implicit val companyPerformingTradeWrites = Json.writes[SubmitDesSubsidiaryPerformingTrade]
  implicit val companyPerformingTradeReads: Reads[SubmitDesSubsidiaryPerformingTrade] = (
    (__ \ "ninetyOwnedModel" \ "ownNinetyPercent").read[String].map(x => answerToBoolean(x)) and
      __.read[SubmitDesCompanyDetailsType]
    ) (SubmitDesSubsidiaryPerformingTrade.apply _)

  implicit val proposedAmountWrites = Json.writes[ProposedAmount]
  implicit val proposedAmountReads: Reads[ProposedAmount] = (
    (__ \ "investmentAmount").read[Int].map(amount=> poundToPence(Right(amount))) and
      ((__ \ "currency").read[String] or Reads.pure("GBP"))
    ) (ProposedAmount.apply _)

  //TODO: unitType is not currently on the model below so defaults - need to read properly once we have it?
  implicit val investmentModelWrites = Json.writes[SubmitDesProposedInvestment]
  implicit val investmentModelReads: Reads[SubmitDesProposedInvestment] = (
    (__ \ "investmentGrowModel" \ "investmentGrowDesc").read[String] and
      ((__ \ "investmentGrowModel" \ "unitType").read[String] or Reads.pure("Shares")) and
      (__ \ "proposedInvestmentModel").read[ProposedAmount]
    ) (SubmitDesProposedInvestment.apply _)

  implicit val annualCostModelWrites = Json.writes[AnnualCostModel]
  implicit val annualCostModelReads: Reads[AnnualCostModel] = (
    (__ \ "periodEnding").read[String] and
      (__ \ "operatingCost").read[CostModel] and
      (__ \ "researchAndDevelopmentCost").read[CostModel]
    ) (AnnualCostModel.apply _)

  implicit val rfiModelWrites = Json.writes[RFIModel]
  implicit val rfiModelReads: Reads[RFIModel] = (
    (__ \ "schemeTypeDesc").read[String] and
      (__ \ "otherSchemeName").readNullable[String] and
      __.read(dateReads) and
      (__ \ "investmentAmount").read[DesCostModel] and
      (__ \ "investmentSpent").readNullable[DesCostModel]
    ) (RFIModel.apply _)

  implicit val turnoverCostModelWrites = Json.writes[TurnoverCostModel]
  implicit val turnoverCostModelReads: Reads[TurnoverCostModel] = (
    (__ \ "periodEnding").read[String] and
      (__ \ "turnover").read[CostModel]
    ) (TurnoverCostModel.apply _)

  implicit val formatCnWrites = Json.writes[SubmitDesContactName]
  implicit val formatCnReads: Reads[SubmitDesContactName] = (
    (__ \ "forename").read[String] and
      (__ \ "surname").read[String]
    ) (SubmitDesContactName.apply _)

  implicit val formatMktInfoWrites = Json.writes[SubmitDesMarketInfo]
  implicit val formatMktInfoReads: Reads[SubmitDesMarketInfo] = (
    (__ \ "newGeographicalMarketModel" \ "isNewGeographicalMarket").read[String].map(x => answerToBoolean(x)) and
      (__ \ "newProductModel" \ "isNewProduct").read[String].map(x => answerToBoolean(x)) and
      (__ \ "marketDescription").readNullable[String]
    ) (SubmitDesMarketInfo.apply _)

  implicit val formatCdWrites = Json.writes[SubmitDesContactDetails]
  implicit val formatCdReads: Reads[SubmitDesContactDetails] = (
    (__ \ "telephoneNumber").readNullable[String] and
      (__ \ "mobileNumber").readNullable[String] and
      (__ \ "faxNumber").readNullable[String] and
      (__ \ "email").readNullable[String]
    ) (SubmitDesContactDetails.apply _)

  implicit val annualTurnoversModelWrites = Json.writes[AnnualTurnoversModel]
  implicit val annualTurnoversModelReads: Reads[AnnualTurnoversModel] = (
    (__ \ "nodatarequired").readNullable[String] and
      __.lazyRead(Reads.seq[TurnoverCostModel](turnoverCostModelReads))
    ) (AnnualTurnoversModel.apply _)

  implicit val annualCostsModelWrites = Json.writes[AnnualCostsModel]
  implicit val annualCostsModelReads: Reads[AnnualCostsModel] = (
    (__ \ "nodatarequired").readNullable[String] and
      __.lazyRead(Reads.seq[AnnualCostModel](annualCostModelReads))
    ) (AnnualCostsModel.apply _)

  implicit val rfiCostsModelWrites = Json.writes[RFICostsModel]
  implicit val rfiCostModelReads: Reads[RFICostsModel] = (
    (__ \ "nodatarequired").readNullable[String] and
      __.lazyRead(Reads.seq[RFIModel](rfiModelReads))
    ) (RFICostsModel.apply _)

  implicit val formatTradeModelWrites = Json.writes[SubmitDesTradeModel]
  implicit val formatTradeModelReads: Reads[SubmitDesTradeModel] = (
    (__ \ "whatWillUseForModel" \ "whatWillUseFor").readNullable[String] and
      (__ \ "natureOfBusinessModel" \ "natureofbusiness").read[String] and
      (__ \ "marketInfo").readNullable[SubmitDesMarketInfo] and
      ((__ \ "dateTradeCommenced").read[String] or Reads.pure("9999-12-31")) and
      (__ \ "annualCosts").readNullable[AnnualCostsModel] and
      (__ \ "annualTurnover").readNullable[AnnualTurnoversModel]) (SubmitDesTradeModel.apply _)

  implicit val formatCorDetailsWrites = Json.writes[SubmitDesCorrespondenceDetails]
  implicit val formatCorDetailsReads: Reads[SubmitDesCorrespondenceDetails] = (
    (__ \ "contactDetailsModel").read[SubmitDesContactName] and
      (__ \ "contactDetailsModel").read[SubmitDesContactDetails] and
      (__ \ "correspondenceAddress").read[SubmitDesAddressType]
    ) (SubmitDesCorrespondenceDetails.apply _)

  implicit val organisationWrites = Json.writes[SubmitDesOrganisation]
  implicit val organisationReads: Reads[SubmitDesOrganisation] = (
    (__ \ "utr").readNullable[String] and
      (__ \ "chrn").readNullable[String] and
      //(__ \ "startDate").read[String] and
      (__ \ "startDate").read(dateReads) and
      (__ \ "firstDateOfCommercialSale").readNullable[String] and
      __.read[SubmitDesCompanyDetailsType] and
      (__ \ "previousRFIs").readNullable[RFICostsModel]
    ) (SubmitDesOrganisation.apply _)

  implicit val formatAAFormatWrites = Json.writes[SubmitDesAdvancedAssurance]
  implicit val formatAAFormatReads: Reads[SubmitDesAdvancedAssurance] = (
    (__ \ "schemeTypes").read[SchemeTypesModel] and
      __.read[SubmitDesTradeModel] and
      __.read[SubmitDesProposedInvestment] and
      (__ \ "subsidiaryPerformingTrade").readNullable[SubmitDesSubsidiaryPerformingTrade] and
      (__ \ "knowledgeIntensive").readNullable[KiModel] and
      (__ \ "organisationDetails").read[SubmitDesOrganisation]
    ) (SubmitDesAdvancedAssurance.apply _)

  implicit val formatAAgFormatWrites = Json.writes[SubmitDesSubmission]
  implicit val formatAAgFormatReads: Reads[SubmitDesSubmission] = (
    (__ \ "nodata").readNullable[String] and
      __.read[SubmitDesAdvancedAssurance]
    ) (SubmitDesSubmission.apply _)

  implicit val formatSubTypeWrites = Json.writes[SubmitDesSubmissionType]
  implicit val formatSubTypeReads: Reads[SubmitDesSubmissionType] = (
    (__ \ "agentReferenceNumber").readNullable[String] and
      __.read[SubmitDesCorrespondenceDetails] and
      (__ \ "organisationType").read[String] and
      __.read[SubmitDesSubmission]
    ) (SubmitDesSubmissionType.apply _)


  implicit val formatAASubmissionWrites = Json.writes[DesSubmitAdvancedAssuranceModel]
  implicit val formatAASubmissionReads: Reads[DesSubmitAdvancedAssuranceModel] = (
    (__ \ "submission" \ "acknowledgementReference").readNullable[String] and
      (__ \ "submission").read[SubmitDesSubmissionType]
    ) (DesSubmitAdvancedAssuranceModel.apply _)
}
