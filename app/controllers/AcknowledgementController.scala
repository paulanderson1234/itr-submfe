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

package controllers

import auth.AuthorisedAndEnrolledForTAVC
import config.{FrontendAppConfig, FrontendAuthConnector}
import common.{Constants, KeystoreKeys}
import connectors.{EnrolmentConnector, KeystoreConnector, SubmissionConnector}
import controllers.Helpers.PreviousSchemesHelper
import models.submission._
import models._
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.{Converters, Validation}

import scala.concurrent.Future

object AcknowledgementController extends AcknowledgementController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait AcknowledgementController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val keyStoreConnector: KeystoreConnector
  val submissionConnector: SubmissionConnector

  //noinspection ScalaStyle
  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    for {
    // minimum required fields to continue
      kiProcModel <- keyStoreConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
      natureOfBusiness <- keyStoreConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness)
      contactDetails <- keyStoreConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
      proposedInvestment <- keyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel](KeystoreKeys.proposedInvestment)
      investmentGrow <- keyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](KeystoreKeys.investmentGrow)
      dateOfIncorporation <- keyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
      // company name also a required field when it is implemented

      // potentially optional or required
      whatWillUseFor <- keyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](KeystoreKeys.whatWillUseFor)
      operatingCosts <- keyStoreConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts)
      subsidiariesSpendInvest <- keyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment)
      subsidiariesNinetyOwned <- keyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](KeystoreKeys.subsidiariesNinetyOwned)
      previousSchemes <- PreviousSchemesHelper.getAllInvestmentFromKeystore(keyStoreConnector)
      commercialSale <- keyStoreConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
      newGeographicalMarket <- keyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket)
      newProduct <- keyStoreConnector.fetchAndGetFormData[NewProductModel](KeystoreKeys.newProduct)
      tenYearPlan <- keyStoreConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan)

      result <- createSubmissionDetailsModel(kiProcModel, natureOfBusiness, contactDetails,
        proposedInvestment, investmentGrow, dateOfIncorporation, whatWillUseFor, subsidiariesSpendInvest, subsidiariesNinetyOwned,
        previousSchemes.toList, commercialSale, newGeographicalMarket, newProduct, tenYearPlan, operatingCosts)
    } yield result
  }


  //noinspection ScalaStyle
  //TODO:
  // 1) MostRecent year used in this function will be passed in (used to calculate period ending for costs and turnover)
  // 2) Address Models (company and correspondence) will need passing in when properly implemented
  // from ETMP lookup) to replace tempAddress used below
  // 3) 5 years Annual turnover costs to be passed as list (or converted to list) when implemented - mapping in place already
  // 4) company name required
  // 5) subsidiary performing trade name is required if subsidiary and needs retrieving
  private def createSubmissionDetailsModel(
                                       //required
                                       kiProcModel: Option[KiProcessingModel],
                                       natOfBusiness: Option[NatureOfBusinessModel],
                                       contactDetails: Option[ContactDetailsModel],
                                       proposedInvestment: Option[ProposedInvestmentModel],
                                       investmentGrowModel: Option[InvestmentGrowModel],
                                       dateOfIncorporation: Option[DateOfIncorporationModel],

                                       // potentially optional or potentially required
                                       whatWillUseFor: Option[WhatWillUseForModel],
                                       subsidiariesSpendInvest: Option[SubsidiariesSpendingInvestmentModel],
                                       subsidiariesNinetyOwned: Option[SubsidiariesNinetyOwnedModel],
                                       previousSchemes: List[PreviousSchemeModel],
                                       commercialSale: Option[CommercialSaleModel],
                                       newGeographicalMarket: Option[NewGeographicalMarketModel],
                                       newProduct: Option[NewProductModel],
                                       tenYearPlan: Option[TenYearPlanModel],
                                       operatingCosts: Option[OperatingCostsModel])
                                             (implicit request: Request[AnyContent]): Future[Result] = {

    // temp values that are not captured yet but required. These should be replaced by passed in values when captured:
    val tempCorrespondenceAddress: AddressModel = AddressModel(addressLine1 = "Contact line 1",
      addressLine2 = "Contact Line 2", addressLine3 = Some("Contact Line 3"), addressLine4 = Some("Contact Line 4"),
      postCode = Some("TF4 5CY"), countryCode = "GB")
    val tempCompanyAddress: AddressModel = AddressModel(addressLine1 = "Company line 1 Ltd",
      addressLine2 = "Company Line 2", addressLine3 = Some("Company Line 3"), addressLine4 = Some("Company Line 4"),
      postCode = Some("TF1 4NY"), countryCode = "GB")
    val tempCompanyName = "Company Name Ltd"
    val tempSubsidiaryTradeName = "Subsidiary Company Name Ltd"
    val tempMostRecentYear = 2015

    (kiProcModel, natOfBusiness, contactDetails, proposedInvestment, investmentGrowModel, dateOfIncorporation) match {
      case (Some(ki), Some(natureBusiness), Some(cntDetail), Some(propInv), Some(howInvGrow), Some(dateIncorp)) => {

        // maybe enhance validation here later (validate Ki and description, validate subsid = yes and ninety etc.)
        val submission = Submission(AdvancedAssuranceSubmissionType(
          agentReferenceNumber = None, acknowledgementReference = None, whatWillUseForModel = whatWillUseFor,
          natureOfBusinessModel = natureBusiness, contactDetailsModel = cntDetail,
          proposedInvestmentModel = propInv, investmentGrowModel = howInvGrow,
          correspondenceAddress = tempCorrespondenceAddress,
          schemeTypes = SchemeTypesModel(eis = true),
          marketInfo = buildMarketInformation(ki, newGeographicalMarket, newProduct),
          annualCosts = if (operatingCosts.nonEmpty)
            Some(Converters.operatingCostsToList(operatingCosts.get, tempMostRecentYear)) else None,
          annualTurnover = None,
          knowledgeIntensive = buildKnowledgeIntensive(ki, tenYearPlan),
          subsidiaryPerformingTrade = buildSubsidiaryPerformingTrade(subsidiariesSpendInvest,
            subsidiariesNinetyOwned, tempSubsidiaryTradeName, tempCompanyAddress),
          organisationDetails = buildOrganisationDetails(commercialSale, dateOfIncorporation.get, tempCompanyName
            , tempCompanyAddress, previousSchemes)
        ))

        val submissionResponseModel = submissionConnector.submitAdvancedAssurance(submission)
        submissionResponseModel.map { submissionResponse =>
          submissionResponse.status match {
            case OK => Ok(views.html.checkAndSubmit.Acknowledgement(submissionResponse.json.as[SubmissionResponse]))
            case _ => InternalServerError
          }
        }
      }

      // inconsistent state send to start
      case (_, _, _, _, _, _) => Future.successful(Redirect(routes.IntroductionController.show()))
    }
  }

  private def buildKnowledgeIntensive(ki: KiProcessingModel, tenYearPlan: Option[TenYearPlanModel]):Option[KiModel] = {
    if (ki.companyAssertsIsKi.getOrElse(false))
      Some(KiModel(skilledEmployeesConditionMet = ki.hasPercentageWithMasters.getOrElse(false),
        innovationConditionMet = if (tenYearPlan.nonEmpty) tenYearPlan.get.tenYearPlanDesc else None,
        kiConditionMet = ki.isKi)) else None
  }

  private def buildMarketInformation(ki: KiProcessingModel, newGeographicalMarket: Option[NewGeographicalMarketModel],
  newProduct: Option[NewProductModel]):Option[SubmitMarketInfoModel] = {

    if (newGeographicalMarket.nonEmpty || newProduct.nonEmpty) Some(SubmitMarketInfoModel(
      newGeographicalMarketModel = newGeographicalMarket.get, newProductModel = newProduct.get)) else None
  }

  private def buildSubsidiaryPerformingTrade(subsidiariesSpendInvest: Option[SubsidiariesSpendingInvestmentModel],
                                                  subsidiariesNinetyOwned: Option[SubsidiariesNinetyOwnedModel],
                                                  tradeName: String,
                                                  tradeAddress: AddressModel):Option[SubsidiaryPerformingTradeModel] = {

    if (subsidiariesSpendInvest.fold("")(_.subSpendingInvestment) == Constants.StandardRadioButtonYesValue)
      Some(SubsidiaryPerformingTradeModel(ninetyOwnedModel = subsidiariesNinetyOwned.get,
        organisationName = tradeName, companyAddress = Some(tradeAddress),
        ctUtr = None, crn = None)) else None
  }

  private def buildOrganisationDetails(commercialSale: Option[CommercialSaleModel],
                                       dateOfIncorporation: DateOfIncorporationModel,
                                       companyName: String,
                                       companyAddress: AddressModel,
                                       previousSchemes: List[PreviousSchemeModel]):OrganisationDetailsModel = {

    OrganisationDetailsModel(utr = None, organisationName = companyName, chrn = None, startDate = dateOfIncorporation,
      firstDateOfCommercialSale = if (commercialSale.fold("")(_.hasCommercialSale) == Constants.StandardRadioButtonYesValue){
        val date = commercialSale.get
        Some(Validation.dateToDesFormat(date.commercialSaleDay.get, date.commercialSaleMonth.get, date.commercialSaleYear.get))
      } else None,
      ctUtr = None, crn = None, companyAddress = Some(companyAddress),
      previousRFIs = if(previousSchemes.nonEmpty) Some(previousSchemes) else None)
  }
}
